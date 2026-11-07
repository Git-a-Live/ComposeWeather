package io.gitalive.composeweather.data.remote

import io.gitalive.composeweather.data.remote.dto.ForecastDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 预报接口。字段名与 Open-Meteo 的变量名一一对应，按需拼接，未请求的变量不会返回。
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int
    ): ForecastDto
}
