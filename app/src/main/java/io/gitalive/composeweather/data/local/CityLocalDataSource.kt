package io.gitalive.composeweather.data.local

import io.gitalive.composeweather.data.local.entity.CityEntity
import io.gitalive.composeweather.domain.model.City
import io.objectbox.BoxStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * 收藏城市的本地读写。数据量等于用户收藏数，全表遍历足够，不额外建索引。
 */
@Singleton
class CityLocalDataSource @Inject constructor(
    private val store: BoxStore
) {

    private val box get() = store.boxFor(CityEntity::class.java)

    suspend fun getAll(): List<City> = withContext(Dispatchers.IO) {
        box.getAll().map { it.toDomain() }
    }

    suspend fun getSelected(): City? = withContext(Dispatchers.IO) {
        box.getAll().firstOrNull { it.isSelected }?.toDomain()
    }

    /** 同经纬度视为同一城市，重复收藏时返回已有记录。 */
    suspend fun insertOrGet(city: City): City = withContext(Dispatchers.IO) {
        val existing = box.getAll().firstOrNull {
            it.latitude == city.latitude && it.longitude == city.longitude
        }
        if (existing != null) {
            existing.toDomain()
        } else {
            val entity = city.toEntity(savedAt = System.currentTimeMillis())
            val id = box.put(entity)
            entity.copy(id = id).toDomain()
        }
    }

    suspend fun remove(cityId: Long) = withContext(Dispatchers.IO) {
        box.remove(cityId)
        Unit
    }

    /** 单选切换放在同一事务内，避免出现两条 isSelected 记录。 */
    suspend fun select(cityId: Long) = withContext(Dispatchers.IO) {
        store.runInTx {
            val all = box.getAll()
            all.forEach { it.isSelected = it.id == cityId }
            box.put(all)
        }
    }

    /**
     * 用最新坐标替换"当前位置"记录，旧记录先删除，避免积累多条。
     * 坐标落在已收藏城市附近时直接复用该条，避免出现"杭州/杭州市"两个标签。
     */
    suspend fun replaceCurrentLocation(city: City): City = withContext(Dispatchers.IO) {
        var newId = 0L
        store.runInTx {
            val all = box.getAll()
            val nearby = all.firstOrNull {
                !it.isCurrentLocation &&
                    abs(it.latitude - city.latitude) < NEARBY_THRESHOLD_DEGREES &&
                    abs(it.longitude - city.longitude) < NEARBY_THRESHOLD_DEGREES
            }
            all.filter { it.isCurrentLocation }.forEach { box.remove(it.id) }
            if (nearby != null) {
                nearby.isCurrentLocation = true
                nearby.name = city.name
                box.put(nearby)
                newId = nearby.id
            } else {
                newId = box.put(
                    CityEntity(
                        name = city.name,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        country = city.country,
                        admin1 = city.admin1,
                        isSelected = false,
                        savedAt = System.currentTimeMillis(),
                        isCurrentLocation = true
                    )
                )
            }
        }
        city.copy(id = newId, isCurrentLocation = true)
    }

    private fun CityEntity.toDomain() = City(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        admin1 = admin1,
        isCurrentLocation = isCurrentLocation
    )

    private fun City.toEntity(savedAt: Long) = CityEntity(
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        admin1 = admin1,
        isSelected = false,
        savedAt = savedAt,
        isCurrentLocation = isCurrentLocation
    )

    private companion object {
        /** 约 5.5km，用于判定"同一个城市"，避免定位后出现重复条目。 */
        const val NEARBY_THRESHOLD_DEGREES = 0.05
    }
}
