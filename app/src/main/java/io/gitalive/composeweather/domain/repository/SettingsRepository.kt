package io.gitalive.composeweather.domain.repository

import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.ThemeMode
import io.gitalive.composeweather.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun settings(): Flow<UserSettings>

    suspend fun setTemperatureUnit(unit: TemperatureUnit)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setAutoRefreshMinutes(minutes: Int)

    suspend fun setAutoLocate(enabled: Boolean)
}
