package io.gitalive.composeweather.di

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.gitalive.composeweather.data.remote.GeocodingApi
import io.gitalive.composeweather.data.remote.OpenMeteoApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private val JSON_MEDIA_TYPE = "application/json".toMediaType()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (debuggable) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

    @Provides
    @Singleton
    @ForecastRetrofit
    fun provideForecastRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(FORECAST_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
        .build()

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(GEOCODING_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
        .build()

    @Provides
    @Singleton
    fun provideOpenMeteoApi(@ForecastRetrofit retrofit: Retrofit): OpenMeteoApi =
        retrofit.create(OpenMeteoApi::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)
}
