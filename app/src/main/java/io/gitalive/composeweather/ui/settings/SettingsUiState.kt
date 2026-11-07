package io.gitalive.composeweather.ui.settings

import io.gitalive.composeweather.core.mvi.UiEffect
import io.gitalive.composeweather.core.mvi.UiIntent
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.ThemeMode
import io.gitalive.composeweather.domain.model.UserSettings

data class SettingsUiState(
    val settings: UserSettings = UserSettings()
)

sealed interface SettingsIntent : UiIntent {
    data class SetUnit(val unit: TemperatureUnit) : SettingsIntent
    data class SetTheme(val mode: ThemeMode) : SettingsIntent
    data class SetAutoRefresh(val minutes: Int) : SettingsIntent
    data class SetAutoLocate(val enabled: Boolean) : SettingsIntent
    data object Back : SettingsIntent
}

sealed interface SettingsEffect : UiEffect {
    data object NavigateBack : SettingsEffect
}
