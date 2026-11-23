package io.gitalive.composeweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.gitalive.composeweather.data.local.entity.CityEntity

/**
 * 收藏城市表访问。数据量等于用户收藏数，全部按全表扫描实现。
 * 查询统一按 id 升序，与列表展示的收藏先后顺序一致。
 */
@Dao
interface CityDao {

    @Query("SELECT * FROM cities ORDER BY id")
    suspend fun getAll(): List<CityEntity>

    @Query("SELECT * FROM cities WHERE isSelected = 1 ORDER BY id LIMIT 1")
    suspend fun getSelected(): CityEntity?

    /** 同经纬度视为同一城市。 */
    @Query("SELECT * FROM cities WHERE latitude = :latitude AND longitude = :longitude ORDER BY id LIMIT 1")
    suspend fun findByCoordinates(latitude: Double, longitude: Double): CityEntity?

    /** 排除当前位置记录，查找阈值范围内已收藏的城市。 */
    @Query(
        "SELECT * FROM cities WHERE isCurrentLocation = 0 " +
            "AND ABS(latitude - :latitude) < :threshold " +
            "AND ABS(longitude - :longitude) < :threshold ORDER BY id LIMIT 1"
    )
    suspend fun findNearby(latitude: Double, longitude: Double, threshold: Double): CityEntity?

    @Insert
    suspend fun insert(entity: CityEntity): Long

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteById(cityId: Long)

    @Query("DELETE FROM cities WHERE isCurrentLocation = 1")
    suspend fun deleteCurrentLocation()

    @Query("UPDATE cities SET isSelected = 0")
    suspend fun clearSelection()

    @Query("UPDATE cities SET isSelected = 1 WHERE id = :cityId")
    suspend fun markSelected(cityId: Long)

    @Query("UPDATE cities SET isCurrentLocation = 1, name = :name WHERE id = :cityId")
    suspend fun markCurrentLocation(cityId: Long, name: String)
}
