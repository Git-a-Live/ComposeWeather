package io.gitalive.composeweather.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.gitalive.composeweather.core.mvi.MviViewModel
import io.gitalive.composeweather.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(SettingsUiState()) {

    init {
        viewModelScope.launch {
            settingsRepository.settings().collect { settings ->
                setState { copy(settings = settings) }
            }
        }
    }

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetUnit -> viewModelScope.launch {
                settingsRepository.setTemperatureUnit(intent.unit)
            }
            is SettingsIntent.SetTheme -> viewModelScope.launch {
                settingsRepository.setThemeMode(intent.mode)
            }
            is SettingsIntent.SetAutoRefresh -> viewModelScope.launch {
                settingsRepository.setAutoRefreshMinutes(intent.minutes)
            }
            is SettingsIntent.SetAutoLocate -> viewModelScope.launch {
                settingsRepository.setAutoLocate(intent.enabled)
            }
            SettingsIntent.Back -> sendEffect(SettingsEffect.NavigateBack)
        }
    }
}
