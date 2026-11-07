package io.gitalive.composeweather.data.repository

import io.gitalive.composeweather.data.local.CityLocalDataSource
import io.gitalive.composeweather.data.local.WeatherCacheLocalDataSource
import io.gitalive.composeweather.data.location.LocationDataSource
import io.gitalive.composeweather.data.mapper.cacheKey
import io.gitalive.composeweather.data.mapper.toWeather
import io.gitalive.composeweather.data.remote.GeocodingApi
import io.gitalive.composeweather.data.remote.OpenMeteoApi
import io.gitalive.composeweather.data.remote.dto.ForecastDto
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.Weather
import io.gitalive.composeweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val forecastApi: OpenMeteoApi,
    private val geocodingApi: GeocodingApi,
    private val cityLocal: CityLocalDataSource,
    private val cacheLocal: WeatherCacheLocalDataSource,
    private val locationDataSource: LocationDataSource,
    private val json: Json
) : WeatherRepository {

    private val cities = MutableStateFlow<List<City>>(emptyList())

    override fun savedCities(): Flow<List<City>> = cities.asStateFlow()

    override suspend fun refreshCities() {
        cities.value = cityLocal.getAll()
    }

    override suspend fun selectCity(cityId: Long) {
        cityLocal.select(cityId)
        refreshCities()
    }

    override suspend fun addCity(city: City): City {
        val saved = cityLocal.insertOrGet(city)
        refreshCities()
        return saved
    }

    override suspend fun removeCity(cityId: Long) {
        cityLocal.remove(cityId)
        refreshCities()
    }

    override suspend fun selectedCity(): City? = cityLocal.getSelected()

    override suspend fun locateCurrentCity(): City? {
        val coordinates = locationDataSource.currentCoordinates() ?: return null
        val name = locationDataSource.resolveCityName(coordinates.latitude, coordinates.longitude)
            ?: CURRENT_LOCATION_NAME
        val city = City(
            name = name,
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            country = "",
            admin1 = null,
            isCurrentLocation = true
        )
        val saved = cityLocal.replaceCurrentLocation(city)
        selectCity(saved.id)
        return saved
    }

    override suspend fun searchCities(query: String): List<City> {
        if (query.isBlank()) return emptyList()
        return geocodingApi.search(
            name = query,
            count = SEARCH_LIMIT,
            language = SEARCH_LANGUAGE,
            format = SEARCH_FORMAT
        ).results.map { dto ->
            City(
                name = dto.name,
                latitude = dto.latitude,
                longitude = dto.longitude,
                country = dto.country,
                admin1 = dto.admin1
            )
        }
    }

    override suspend fun loadWeather(city: City, forceRefresh: Boolean): Weather {
        val key = city.cacheKey()
        val cached = cacheLocal.read(key)
        if (!forceRefresh && cached != null && isFresh(cached.updatedAt)) {
            decode(cached.payload, city, cached.updatedAt)?.let { return it }
        }
        return try {
            val dto = forecastApi.forecast(
                latitude = city.latitude,
                longitude = city.longitude,
                current = CURRENT_FIELDS,
                hourly = HOURLY_FIELDS,
                daily = DAILY_FIELDS,
                timezone = TIMEZONE_AUTO,
                forecastDays = FORECAST_DAYS
            )
            val updatedAt = System.currentTimeMillis()
            val weather = dto.toWeather(city, updatedAt)
                ?: throw IllegalStateException("天气报文缺少必要字段")
            cacheLocal.write(key, json.encodeToString(dto), updatedAt)
            weather
        } catch (throwable: Exception) {
            // 网络失败时回退到已过期缓存，仍无缓存才把异常抛给上层
            val fallback = cacheLocal.read(key) ?: throw throwable
            decode(fallback.payload, city, fallback.updatedAt) ?: throw throwable
        }
    }

    private fun decode(payload: String, city: City, updatedAt: Long): Weather? = runCatching {
        json.decodeFromString<ForecastDto>(payload).toWeather(city, updatedAt)
    }.getOrNull()

    private fun isFresh(updatedAt: Long): Boolean =
        System.currentTimeMillis() - updatedAt < CACHE_TTL_MILLIS

    private companion object {
        const val CACHE_TTL_MILLIS = 30 * 60 * 1000L
        const val CURRENT_LOCATION_NAME = "当前位置"
        const val SEARCH_LIMIT = 15
        const val SEARCH_LANGUAGE = "zh"
        const val SEARCH_FORMAT = "json"
        const val TIMEZONE_AUTO = "auto"
        const val FORECAST_DAYS = 7
        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation," +
                "weather_code,surface_pressure,wind_speed_10m"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max," +
                "uv_index_max,sunrise,sunset"
    }
}
