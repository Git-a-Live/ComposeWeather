package io.gitalive.composeweather.data.remote

import io.gitalive.composeweather.data.remote.dto.GeocodingDto
import retrofit2.http.GET
import retrofit2.http.Query

/** 城市检索接口，与预报接口不同域，单独一个 Retrofit 实例。 */
interface GeocodingApi {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String,
        @Query("format") format: String
    ): GeocodingDto
}
