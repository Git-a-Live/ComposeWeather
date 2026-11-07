package io.gitalive.composeweather.data.local.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * 天气缓存。payload 为 Open-Meteo 原始响应 JSON，避免为每种字段变更做数据库迁移。
 * cityKey 由经纬度拼成，保留 4 位小数，与展示精度一致。
 */
@Entity
data class WeatherCacheEntity(
    @Id var id: Long = 0L,
    var cityKey: String = "",
    var payload: String = "",
    var updatedAt: Long = 0L
)
