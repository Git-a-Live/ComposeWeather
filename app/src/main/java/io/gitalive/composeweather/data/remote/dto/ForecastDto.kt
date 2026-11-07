package io.gitalive.composeweather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo /v1/forecast 响应。
 * 所有字段可空：接口在不同模式下可能省略未请求的字段。
 */
@Serializable
data class ForecastDto(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("current") val current: CurrentDto? = null,
    @SerialName("hourly") val hourly: HourlyDto? = null,
    @SerialName("daily") val daily: DailyDto? = null
)

@Serializable
data class CurrentDto(
    @SerialName("time") val time: String? = null,
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("relative_humidity_2m") val humidity: Int? = null,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerialName("is_day") val isDay: Int? = null,
    @SerialName("precipitation") val precipitation: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("surface_pressure") val pressure: Double? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null
)

@Serializable
data class HourlyDto(
    @SerialName("time") val time: List<String> = emptyList(),
    @SerialName("temperature_2m") val temperature: List<Double?> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList()
)

@Serializable
data class DailyDto(
    @SerialName("time") val time: List<String> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max") val maxTemperature: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val minTemperature: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
    @SerialName("sunrise") val sunrise: List<String> = emptyList(),
    @SerialName("sunset") val sunset: List<String> = emptyList()
)
