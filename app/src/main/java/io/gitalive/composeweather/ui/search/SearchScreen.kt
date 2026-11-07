package io.gitalive.composeweather.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.gitalive.composeweather.domain.model.City

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                SearchEffect.NavigateBack -> onBack()
            }
        }
    }

    SearchScreen(
        state = state,
        onIntent = viewModel::dispatch,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("城市") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(SearchIntent.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                placeholder = { Text("输入城市名，例如 上海 / Tokyo") },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onIntent(SearchIntent.ClearQuery) }) {
                            Icon(Icons.Filled.Close, contentDescription = "清空")
                        }
                    }
                }
            )

            if (state.isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                if (state.savedCities.isNotEmpty()) {
                    item { SectionHeader("收藏城市") }
                    items(state.savedCities, key = { it.id }) { city ->
                        CityRow(
                            city = city,
                            trailing = {
                                IconButton(onClick = { onIntent(SearchIntent.RemoveCity(city)) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "移除")
                                }
                            }
                        )
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                }

                if (state.results.isNotEmpty()) {
                    item { SectionHeader("搜索结果") }
                    items(state.results, key = { "${it.name}-${it.latitude}-${it.longitude}" }) { city ->
                        CityRow(
                            city = city,
                            onClick = { onIntent(SearchIntent.SaveCity(city)) },
                            trailing = {
                                IconButton(onClick = { onIntent(SearchIntent.SaveCity(city)) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "收藏")
                                }
                            }
                        )
                    }
                }

                if (state.results.isEmpty() && state.savedCities.isEmpty() && state.query.isNotBlank() && !state.isSearching) {
                    item {
                        Text(
                            text = "没有找到匹配的城市",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun CityRow(
    city: City,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = city.name, style = MaterialTheme.typography.bodyLarge)
            val subtitle = listOfNotNull(city.admin1, city.country)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing()
    }
}
