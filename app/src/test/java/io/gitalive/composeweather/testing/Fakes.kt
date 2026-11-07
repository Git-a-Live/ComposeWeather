package io.gitalive.composeweather.testing

import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.CurrentWeather
import io.gitalive.composeweather.domain.model.DailyWeather
import io.gitalive.composeweather.domain.model.HourlyWeather
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.ThemeMode
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.domain.model.Weather
import io.gitalive.composeweather.domain.repository.SettingsRepository
import io.gitalive.composeweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

val SampleCity = City(
    id = 1L,
    name = "杭州",
    latitude = 30.2741,
    longitude = 120.1551,
    country = "中国",
    admin1 = "浙江省"
)

fun sampleWeather(city: City = SampleCity): Weather = Weather(
    city = city,
    current = CurrentWeather(
        temperature = 19.4,
        apparentTemperature = 17.2,
        humidity = 60,
        weatherCode = 3,
        windSpeed = 15.9,
        pressure = 1021.0,
        precipitation = 0.0,
        isDay = true
    ),
    hourly = (0 until 24).map { index ->
        HourlyWeather(
            time = "2026-11-%02dT%02d:00".format(7 + index / 24, index % 24),
            temperature = 18.0 + index % 5,
            weatherCode = 3,
            precipitationProbability = 20
        )
    },
    daily = (0 until 7).map { index ->
        DailyWeather(
            date = "2026-11-%02d".format(7 + index),
            weatherCode = 3,
            maxTemperature = 19.0,
            minTemperature = 13.0,
            precipitationProbability = 100,
            uvIndexMax = 0.6,
            sunrise = "06:23",
            sunset = "17:10"
        )
    },
    updatedAt = 1_700_000_000_000L
)

/** 只关心调用记录与可控失败，不做真实持久化。 */
class FakeWeatherRepository : WeatherRepository {

    val citiesFlow = MutableStateFlow<List<City>>(emptyList())
    val loadedCities = mutableListOf<City>()

    var weather: Weather = sampleWeather()
    var failure: Throwable? = null
    var locatedCity: City? = null

    private var selectedId: Long? = null

    override fun savedCities(): Flow<List<City>> = citiesFlow.asStateFlow()

    override suspend fun refreshCities() = Unit

    override suspend fun selectCity(cityId: Long) {
        selectedId = cityId
    }

    override suspend fun addCity(city: City): City {
        val saved = city.copy(id = (citiesFlow.value.maxOfOrNull { it.id } ?: 0L) + 1)
        citiesFlow.value = citiesFlow.value + saved
        return saved
    }

    override suspend fun removeCity(cityId: Long) {
        citiesFlow.value = citiesFlow.value.filterNot { it.id == cityId }
    }

    override suspend fun selectedCity(): City? =
        citiesFlow.value.firstOrNull { it.id == selectedId } ?: citiesFlow.value.firstOrNull()

    override suspend fun locateCurrentCity(): City? = locatedCity

    override suspend fun searchCities(query: String): List<City> = emptyList()

    override suspend fun loadWeather(city: City, forceRefresh: Boolean): Weather {
        loadedCities += city
        failure?.let { throw it }
        return weather.copy(city = city)
    }
}

class FakeSettingsRepository(
    initial: UserSettings = UserSettings(autoRefreshMinutes = 0)
) : SettingsRepository {

    val state = MutableStateFlow(initial)

    override fun settings(): Flow<UserSettings> = state.asStateFlow()

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        state.update { it.copy(temperatureUnit = unit) }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.update { it.copy(themeMode = mode) }
    }

    override suspend fun setAutoRefreshMinutes(minutes: Int) {
        state.update { it.copy(autoRefreshMinutes = minutes) }
    }

    override suspend fun setAutoLocate(enabled: Boolean) {
        state.update { it.copy(autoLocate = enabled) }
    }
}
