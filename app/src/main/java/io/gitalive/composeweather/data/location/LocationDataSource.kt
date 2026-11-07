package io.gitalive.composeweather.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 系统定位与坐标反查。全部走 AOSP 的 LocationManager + Geocoder，
 * 不依赖 Play Services，也不需要第三方 Key；国内无 Google 服务时 Geocoder 可能返回空，
 * 调用方需接受 name 为 null 并退化为"当前位置"。
 */
@Singleton
class LocationDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class Coordinates(val latitude: Double, val longitude: Double)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * 优先取一次实时定位，超时（或 provider 无响应）则退回上次已知位置。
     * 网络 provider 优先，室内也能出结果；未开启定位时返回 null。
     */
    @SuppressLint("MissingPermission")
    suspend fun currentCoordinates(): Coordinates? = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext null
        val manager = context.getSystemService(LocationManager::class.java) ?: return@withContext null
        val provider = when {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> return@withContext null
        }

        val lastKnown = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        val fresh = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val callback = Consumer<Location> { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                manager.getCurrentLocation(
                    provider,
                    null,
                    ContextCompat.getMainExecutor(context),
                    callback
                )
            }
        }
        (fresh ?: lastKnown)?.let { Coordinates(it.latitude, it.longitude) }
    }

    /** 城市名，取 locality → subAdminArea → adminArea 的第一个非空值。 */
    suspend fun resolveCityName(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return withTimeoutOrNull(GEOCODE_TIMEOUT_MILLIS) {
            val addresses: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { results ->
                        if (continuation.isActive) continuation.resume(results)
                    }
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1).orEmpty()
                }
            }
            addresses.firstOrNull()?.let { address ->
                listOfNotNull(address.locality, address.subAdminArea, address.adminArea)
                    .firstOrNull { it.isNotBlank() }
            }
        }
    }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 5_000L
        const val GEOCODE_TIMEOUT_MILLIS = 3_000L
    }
}
