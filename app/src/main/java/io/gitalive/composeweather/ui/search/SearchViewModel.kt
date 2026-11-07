package io.gitalive.composeweather.ui.search

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.gitalive.composeweather.core.mvi.MviViewModel
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.repository.WeatherRepository
import io.gitalive.composeweather.ui.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: WeatherRepository
) : MviViewModel<SearchUiState, SearchIntent, SearchEffect>(SearchUiState()) {

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.refreshCities()
            repository.savedCities().collect { cities ->
                setState { copy(savedCities = cities) }
            }
        }
    }

    override fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> onQueryChanged(intent.query)
            is SearchIntent.SaveCity -> save(intent.city)
            is SearchIntent.RemoveCity -> remove(intent.city)
            SearchIntent.ClearQuery -> {
                searchJob?.cancel()
                setState { copy(query = "", results = emptyList(), isSearching = false) }
            }
            SearchIntent.Back -> sendEffect(SearchEffect.NavigateBack)
        }
    }

    /** 输入停止 350ms 后再发请求，避免逐字触发地理编码接口。 */
    private fun onQueryChanged(query: String) {
        setState { copy(query = query, errorMessage = null) }
        searchJob?.cancel()
        if (query.isBlank()) {
            setState { copy(results = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            setState { copy(isSearching = true) }
            runCatching { repository.searchCities(query.trim()) }
                .onSuccess { cities -> setState { copy(results = cities, isSearching = false) } }
                .onFailure { throwable ->
                    setState {
                        copy(
                            isSearching = false,
                            errorMessage = throwable.toUserMessage(),
                            results = emptyList()
                        )
                    }
                }
        }
    }

    private fun save(city: City) {
        viewModelScope.launch {
            val saved = repository.addCity(city)
            sendEffect(SearchEffect.ShowMessage("${saved.name} 已加入收藏"))
        }
    }

    private fun remove(city: City) {
        viewModelScope.launch {
            repository.removeCity(city.id)
            sendEffect(SearchEffect.ShowMessage("${city.name} 已移除"))
        }
    }

    private companion object {
        const val DEBOUNCE_MILLIS = 350L
    }
}
