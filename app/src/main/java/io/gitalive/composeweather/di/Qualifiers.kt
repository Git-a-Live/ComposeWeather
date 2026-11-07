package io.gitalive.composeweather.di

import javax.inject.Qualifier

/** 预报接口（api.open-meteo.com）专用实例。 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForecastRetrofit

/** 城市检索接口（geocoding-api.open-meteo.com）专用实例。 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit
