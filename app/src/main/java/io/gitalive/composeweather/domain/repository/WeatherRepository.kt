package io.gitalive.composeweather.domain.repository

import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    /** 收藏城市列表，随增删实时更新。 */
    fun savedCities(): Flow<List<City>>

    suspend fun refreshCities()

    suspend fun selectCity(cityId: Long)

    suspend fun addCity(city: City): City

    suspend fun removeCity(cityId: Long)

    suspend fun selectedCity(): City?

    /**
     * 读取当前定位并作为"当前位置"城市入库、选中。
     * 无定位权限、定位关闭或取不到坐标时返回 null。
     */
    suspend fun locateCurrentCity(): City?

    suspend fun searchCities(query: String): List<City>

    /**
     * 拉取天气。非强制刷新时优先读未过期缓存；网络异常时回退到过期缓存。
     * @throws java.io.IOException 网络层失败且无任何缓存可用
     */
    suspend fun loadWeather(city: City, forceRefresh: Boolean): Weather
}
