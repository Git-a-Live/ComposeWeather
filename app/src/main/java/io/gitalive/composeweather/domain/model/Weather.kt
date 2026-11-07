package io.gitalive.composeweather.domain.model

/** 城市坐标，id 为 ObjectBox 主键，未入库时为 0。 */
data class City(
    val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val admin1: String? = null,
    val isCurrentLocation: Boolean = false
)

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val weatherCode: Int,
    val windSpeed: Double,
    val pressure: Double,
    val precipitation: Double,
    val isDay: Boolean
)

data class HourlyWeather(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int
)

data class DailyWeather(
    val date: String,
    val weatherCode: Int,
    val maxTemperature: Double,
    val minTemperature: Double,
    val precipitationProbability: Int,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String
)

/** 温度统一按摄氏度存放，展示层按用户设置换算。 */
data class Weather(
    val city: City,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val updatedAt: Long
)
