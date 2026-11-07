package io.gitalive.composeweather.data.local.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/** 收藏城市。isSelected 标记当前展示城市，同一时刻只有一条为 true。 */
@Entity
data class CityEntity(
    @Id var id: Long = 0L,
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var country: String = "",
    var admin1: String? = null,
    var isSelected: Boolean = false,
    var savedAt: Long = 0L,
    var isCurrentLocation: Boolean = false
)
