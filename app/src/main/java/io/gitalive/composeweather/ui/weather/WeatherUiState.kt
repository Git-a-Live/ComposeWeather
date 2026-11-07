package io.gitalive.composeweather.ui.weather

import io.gitalive.composeweather.core.mvi.UiEffect
import io.gitalive.composeweather.core.mvi.UiIntent
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.domain.model.Weather

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLocating: Boolean = false,
    val weather: Weather? = null,
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val settings: UserSettings = UserSettings(),
    val errorMessage: String? = null
)

sealed interface WeatherIntent : UiIntent {
    data object Load : WeatherIntent
    data object Refresh : WeatherIntent
    data class SelectCity(val city: City) : WeatherIntent
    data object LocateCurrent : WeatherIntent
    data object Retry : WeatherIntent
    data object DismissError : WeatherIntent
}

sealed interface WeatherEffect : UiEffect {
    data class ShowMessage(val message: String) : WeatherEffect
}
