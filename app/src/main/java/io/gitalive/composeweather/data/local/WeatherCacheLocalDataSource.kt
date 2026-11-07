package io.gitalive.composeweather.data.local

import io.gitalive.composeweather.data.local.entity.WeatherCacheEntity
import io.objectbox.BoxStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 天气报文缓存。缓存条数等于收藏城市数，全表遍历成本可忽略。
 */
@Singleton
class WeatherCacheLocalDataSource @Inject constructor(
    private val store: BoxStore
) {

    private val box get() = store.boxFor(WeatherCacheEntity::class.java)

    suspend fun read(cityKey: String): WeatherCacheEntity? = withContext(Dispatchers.IO) {
        box.getAll().firstOrNull { it.cityKey == cityKey }
    }

    suspend fun write(cityKey: String, payload: String, updatedAt: Long) = withContext(Dispatchers.IO) {
        val existing = box.getAll().firstOrNull { it.cityKey == cityKey }
        box.put(
            existing?.copy(payload = payload, updatedAt = updatedAt)
                ?: WeatherCacheEntity(cityKey = cityKey, payload = payload, updatedAt = updatedAt)
        )
        Unit
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        box.removeAll()
    }
}
