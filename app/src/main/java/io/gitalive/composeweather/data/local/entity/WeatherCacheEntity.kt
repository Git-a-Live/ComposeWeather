package io.gitalive.composeweather.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 天气缓存。payload 为 Open-Meteo 原始响应 JSON，避免为每种字段变更做数据库迁移。
 * cityKey 由经纬度拼成，保留 4 位小数，与展示精度一致。
 * cityKey 建唯一索引，写入时按冲突替换，保证同一城市只留一条缓存。
 */
@Entity(tableName = "weather_cache", indices = [Index(value = ["cityKey"], unique = true)])
data class WeatherCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cityKey: String = "",
    val payload: String = "",
    val updatedAt: Long = 0L
)

/*
 * ObjectBox 版本，已由 Room 取代，保留作对照：
 *
 * import io.objectbox.annotation.Entity
 * import io.objectbox.annotation.Id
 *
 * @Entity
 * data class WeatherCacheEntity(
 *     @Id var id: Long = 0L,
 *     var cityKey: String = "",
 *     var payload: String = "",
 *     var updatedAt: Long = 0L
 * )
 */
