package io.gitalive.composeweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 收藏城市。isSelected 标记当前展示城市，同一时刻只有一条为 true。 */
@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val admin1: String? = null,
    val isSelected: Boolean = false,
    val savedAt: Long = 0L,
    val isCurrentLocation: Boolean = false
)

/*
 * ObjectBox 版本，已由 Room 取代，保留作对照：
 *
 * import io.objectbox.annotation.Entity
 * import io.objectbox.annotation.Id
 *
 * @Entity
 * data class CityEntity(
 *     @Id var id: Long = 0L,
 *     var name: String = "",
 *     var latitude: Double = 0.0,
 *     var longitude: Double = 0.0,
 *     var country: String = "",
 *     var admin1: String? = null,
 *     var isSelected: Boolean = false,
 *     var savedAt: Long = 0L,
 *     var isCurrentLocation: Boolean = false
 * )
 */
