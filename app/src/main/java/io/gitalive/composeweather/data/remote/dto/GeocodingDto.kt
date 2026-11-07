package io.gitalive.composeweather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** geocoding-api.open-meteo.com /v1/search 响应，无匹配时 results 字段整体缺失。 */
@Serializable
data class GeocodingDto(
    @SerialName("results") val results: List<CityDto> = emptyList()
)

@Serializable
data class CityDto(
    @SerialName("id") val id: Long = 0L,
    @SerialName("name") val name: String = "",
    @SerialName("latitude") val latitude: Double = 0.0,
    @SerialName("longitude") val longitude: Double = 0.0,
    @SerialName("country") val country: String = "",
    @SerialName("admin1") val admin1: String? = null
)
