package io.gitalive.composeweather.ui.weather

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.gitalive.composeweather.core.mvi.MviViewModel
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.DefaultCity
import io.gitalive.composeweather.domain.repository.SettingsRepository
import io.gitalive.composeweather.domain.repository.WeatherRepository
import io.gitalive.composeweather.ui.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : MviViewModel<WeatherUiState, WeatherIntent, WeatherEffect>(WeatherUiState()) {

    private var autoRefreshJob: Job? = null

    init {
        observeCities()
        observeSettings()
        bootstrap()
    }

    override fun handleIntent(intent: WeatherIntent) {
        when (intent) {
            WeatherIntent.Load -> currentState.selectedCity?.let { loadWeather(it) }
            WeatherIntent.Refresh -> currentState.selectedCity?.let { loadWeather(it, forceRefresh = true) }
            WeatherIntent.Retry -> currentState.selectedCity?.let { loadWeather(it) }
            is WeatherIntent.SelectCity -> selectCity(intent.city)
            WeatherIntent.LocateCurrent -> locateCurrent()
            WeatherIntent.DismissError -> setState { copy(errorMessage = null) }
        }
    }

    /**
     * 启动顺序：自动定位开着且能取到坐标时优先用当前位置，否则回落到上次选中的城市，
     * 首次安装没有任何收藏时写入默认城市。
     */
    private fun bootstrap() {
        viewModelScope.launch {
            repository.refreshCities()
            val autoLocate = settingsRepository.settings().first().autoLocate
            if (autoLocate) {
                val located = runCatching { repository.locateCurrentCity() }.getOrNull()
                if (located != null) {
                    loadWeather(located)
                    return@launch
                }
            }
            val cities = repository.savedCities().first()
            if (cities.isEmpty()) {
                val created = repository.addCity(DefaultCity)
                repository.selectCity(created.id)
                loadWeather(created)
            } else {
                val selected = repository.selectedCity() ?: cities.first()
                loadWeather(selected)
            }
        }
    }

    private fun locateCurrent() {
        viewModelScope.launch {
            setState { copy(isLocating = true) }
            runCatching { repository.locateCurrentCity() }
                .onSuccess { city ->
                    setState { copy(isLocating = false) }
                    if (city == null) {
                        sendEffect(WeatherEffect.ShowMessage("定位失败，请确认系统定位已开启"))
                    } else {
                        loadWeather(city)
                    }
                }
                .onFailure { throwable ->
                    setState { copy(isLocating = false) }
                    sendEffect(WeatherEffect.ShowMessage(throwable.toUserMessage()))
                }
        }
    }

    private fun observeCities() {
        viewModelScope.launch {
            repository.savedCities().collect { cities ->
                setState { copy(cities = cities) }
                if (currentState.selectedCity == null) {
                    cities.firstOrNull()?.let { setState { copy(selectedCity = it) } }
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings().collect { settings ->
                setState { copy(settings = settings) }
                restartAutoRefresh(settings.autoRefreshMinutes)
            }
        }
    }

    private fun selectCity(city: City) {
        viewModelScope.launch {
            repository.selectCity(city.id)
            setState { copy(selectedCity = city) }
            loadWeather(city)
        }
    }

    private fun loadWeather(city: City, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            setState {
                copy(
                    selectedCity = city,
                    isLoading = weather == null,
                    isRefreshing = weather != null,
                    errorMessage = null
                )
            }
            runCatching { repository.loadWeather(city, forceRefresh) }
                .onSuccess { weather ->
                    setState { copy(weather = weather, isLoading = false, isRefreshing = false) }
                }
                .onFailure { throwable ->
                    val message = throwable.toUserMessage()
                    val hasData = currentState.weather != null
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = if (hasData) null else message
                        )
                    }
                    if (hasData) sendEffect(WeatherEffect.ShowMessage(message))
                }
        }
    }

    private fun restartAutoRefresh(minutes: Int) {
        autoRefreshJob?.cancel()
        if (minutes <= 0) return
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(minutes * 60_000L)
                currentState.selectedCity?.let { loadWeather(it, forceRefresh = true) }
            }
        }
    }
}
