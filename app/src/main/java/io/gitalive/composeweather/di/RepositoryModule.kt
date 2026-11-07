package io.gitalive.composeweather.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.gitalive.composeweather.data.local.SettingsDataStore
import io.gitalive.composeweather.data.repository.WeatherRepositoryImpl
import io.gitalive.composeweather.domain.repository.SettingsRepository
import io.gitalive.composeweather.domain.repository.WeatherRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository
}
