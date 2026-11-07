package io.gitalive.composeweather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 承载全局设置，供主题切换使用。 */
@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())
}
