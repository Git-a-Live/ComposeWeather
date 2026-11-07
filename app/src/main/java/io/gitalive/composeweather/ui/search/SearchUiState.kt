package io.gitalive.composeweather.ui.search

import io.gitalive.composeweather.core.mvi.UiEffect
import io.gitalive.composeweather.core.mvi.UiIntent
import io.gitalive.composeweather.domain.model.City

data class SearchUiState(
    val query: String = "",
    val results: List<City> = emptyList(),
    val savedCities: List<City> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SearchIntent : UiIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data class SaveCity(val city: City) : SearchIntent
    data class RemoveCity(val city: City) : SearchIntent
    data object ClearQuery : SearchIntent
    data object Back : SearchIntent
}

sealed interface SearchEffect : UiEffect {
    data class ShowMessage(val message: String) : SearchEffect
    data object NavigateBack : SearchEffect
}
