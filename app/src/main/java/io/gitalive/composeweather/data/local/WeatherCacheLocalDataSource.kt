package io.gitalive.composeweather.data.local

import io.gitalive.composeweather.data.local.dao.WeatherCacheDao
import io.gitalive.composeweather.data.local.entity.WeatherCacheEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 天气报文缓存。缓存条数等于收藏城市数，按 cityKey 唯一索引定位，不再全表遍历。
 */
@Singleton
class WeatherCacheLocalDataSource @Inject constructor(
    private val weatherCacheDao: WeatherCacheDao
) {

    suspend fun read(cityKey: String): WeatherCacheEntity? = weatherCacheDao.read(cityKey)

    /** cityKey 已存在时由冲突替换覆盖整行，行内主键会重新分配。 */
    suspend fun write(cityKey: String, payload: String, updatedAt: Long) {
        weatherCacheDao.write(
            WeatherCacheEntity(cityKey = cityKey, payload = payload, updatedAt = updatedAt)
        )
    }

    suspend fun clear() {
        weatherCacheDao.clear()
    }
}

// ---------------------------------------------------------------------------
// ObjectBox 版本，已由 Room 取代，保留作对照。
// 与上面的差异：全表遍历查 cityKey 改为唯一索引定位，写入由"先查后覆盖"改为冲突替换。
//
// package io.gitalive.composeweather.data.local
//
// import io.gitalive.composeweather.data.local.entity.WeatherCacheEntity
// import io.objectbox.BoxStore
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext
// import javax.inject.Inject
// import javax.inject.Singleton
//
// /**
//  * 天气报文缓存。缓存条数等于收藏城市数，全表遍历成本可忽略。
//  */
// @Singleton
// class WeatherCacheLocalDataSource @Inject constructor(
//     private val store: BoxStore
// ) {
//
//     private val box get() = store.boxFor(WeatherCacheEntity::class.java)
//
//     suspend fun read(cityKey: String): WeatherCacheEntity? = withContext(Dispatchers.IO) {
//         box.getAll().firstOrNull { it.cityKey == cityKey }
//     }
//
//     suspend fun write(cityKey: String, payload: String, updatedAt: Long) = withContext(Dispatchers.IO) {
//         val existing = box.getAll().firstOrNull { it.cityKey == cityKey }
//         box.put(
//             existing?.copy(payload = payload, updatedAt = updatedAt)
//                 ?: WeatherCacheEntity(cityKey = cityKey, payload = payload, updatedAt = updatedAt)
//         )
//         Unit
//     }
//
//     suspend fun clear() = withContext(Dispatchers.IO) {
//         box.removeAll()
//     }
// }
// ---------------------------------------------------------------------------
