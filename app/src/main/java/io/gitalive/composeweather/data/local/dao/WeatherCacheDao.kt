package io.gitalive.composeweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.gitalive.composeweather.data.local.entity.WeatherCacheEntity

/**
 * 天气报文缓存表访问。缓存条数等于收藏城市数，按 cityKey 唯一索引定位。
 */
@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE cityKey = :cityKey LIMIT 1")
    suspend fun read(cityKey: String): WeatherCacheEntity?

    /** cityKey 命中唯一索引时整行替换，主键会重新分配，对外不可见。 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun write(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clear()
}
