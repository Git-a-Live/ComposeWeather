package io.gitalive.composeweather.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.ThemeMode
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** 用户设置持久化。枚举按 name 存字符串，新增枚举值不影响已存数据。 */
@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_REFRESH = intPreferencesKey("auto_refresh_minutes")
        val AUTO_LOCATE = booleanPreferencesKey("auto_locate")
    }

    override fun settings(): Flow<UserSettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { preferences ->
            UserSettings(
                temperatureUnit = preferences[Keys.TEMPERATURE_UNIT]
                    ?.let { stored -> TemperatureUnit.entries.firstOrNull { it.name == stored } }
                    ?: TemperatureUnit.CELSIUS,
                themeMode = preferences[Keys.THEME_MODE]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.SYSTEM,
                autoRefreshMinutes = preferences[Keys.AUTO_REFRESH] ?: DEFAULT_REFRESH_MINUTES,
                autoLocate = preferences[Keys.AUTO_LOCATE] ?: true
            )
        }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[Keys.TEMPERATURE_UNIT] = unit.name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setAutoRefreshMinutes(minutes: Int) {
        dataStore.edit { it[Keys.AUTO_REFRESH] = minutes }
    }

    override suspend fun setAutoLocate(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_LOCATE] = enabled }
    }

    private companion object {
        const val DEFAULT_REFRESH_MINUTES = 30
    }
}
