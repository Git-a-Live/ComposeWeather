package io.gitalive.composeweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.gitalive.composeweather.data.local.dao.CityDao
import io.gitalive.composeweather.data.local.dao.WeatherCacheDao
import io.gitalive.composeweather.data.local.entity.CityEntity
import io.gitalive.composeweather.data.local.entity.WeatherCacheEntity

/**
 * 本地库。version 与 schemas/ 下导出的 schema 配套，改表结构时递增版本并补迁移。
 */
@Database(
    entities = [CityEntity::class, WeatherCacheEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao

    abstract fun weatherCacheDao(): WeatherCacheDao
}
