package io.gitalive.composeweather.ui.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.Weather
import io.gitalive.composeweather.domain.model.toDisplay
import io.gitalive.composeweather.ui.components.DailyForecastList
import io.gitalive.composeweather.ui.components.GlassCard
import io.gitalive.composeweather.ui.components.HourlyForecastRow
import io.gitalive.composeweather.ui.components.TemperatureChart
import io.gitalive.composeweather.ui.components.WeatherBackground
import io.gitalive.composeweather.ui.components.WeatherGlyphIcon
import io.gitalive.composeweather.ui.util.WeatherGlyph
import io.gitalive.composeweather.ui.util.paletteFor
import io.gitalive.composeweather.ui.util.weatherCodeToGlyph
import io.gitalive.composeweather.ui.util.weatherCodeToLabel
import io.gitalive.composeweather.widget.WeatherWidget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherRoute(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result.values.any { it }
        if (hasLocationPermission) {
            viewModel.dispatch(WeatherIntent.LocateCurrent)
        }
    }

    // 首次进入且开启自动定位时只尝试一次，被拒绝后不再反复弹窗，改由首页提示条引导
    var autoLocateHandled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.settings.autoLocate, hasLocationPermission) {
        if (autoLocateHandled || !state.settings.autoLocate) return@LaunchedEffect
        autoLocateHandled = true
        if (hasLocationPermission) {
            viewModel.dispatch(WeatherIntent.LocateCurrent)
        } else {
            permissionLauncher.launch(LocationPermissions)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WeatherEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // 应用侧拿到新数据后同步刷新桌面小组件，避免桌面长期停留在旧数据
    LaunchedEffect(state.weather?.updatedAt) {
        if (state.weather != null) {
            WeatherWidget().updateAll(context)
        }
    }

    val requestLocation: () -> Unit = {
        if (hasLocationPermission) {
            viewModel.dispatch(WeatherIntent.LocateCurrent)
        } else {
            permissionLauncher.launch(LocationPermissions)
        }
    }

    WeatherScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onOpenSearch = onOpenSearch,
        onOpenSettings = onOpenSettings,
        hasLocationPermission = hasLocationPermission,
        onRequestLocation = requestLocation,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun WeatherScreen(
    state: WeatherUiState,
    onIntent: (WeatherIntent) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    hasLocationPermission: Boolean = true,
    onRequestLocation: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val weather = state.weather
    val glyph = weather
        ?.let { weatherCodeToGlyph(it.current.weatherCode, it.current.isDay) }
        ?: WeatherGlyph.CLOUD
    val contentColor = paletteFor(glyph).content

    WeatherBackground(glyph = glyph) {
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(WeatherIntent.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TopBar(
                            city = state.selectedCity,
                            contentColor = contentColor,
                            isLocating = state.isLocating,
                            onLocate = onRequestLocation,
                            onOpenSearch = onOpenSearch,
                            onOpenSettings = onOpenSettings
                        )
                    }
                    if (state.settings.autoLocate && !hasLocationPermission) {
                        item {
                            LocationPromptCard(
                                contentColor = contentColor,
                                onRequest = onRequestLocation
                            )
                        }
                    }
                    if (state.cities.size > 1) {
                        item {
                            CityChips(
                                cities = state.cities,
                                selectedId = state.selectedCity?.id,
                                contentColor = contentColor,
                                onSelect = { onIntent(WeatherIntent.SelectCity(it)) }
                            )
                        }
                    }
                    if (weather != null) {
                        item {
                            CurrentBlock(
                                weather = weather,
                                unit = state.settings.temperatureUnit,
                                contentColor = contentColor
                            )
                        }
                        alertFor(weather)?.let { alert ->
                            item { AlertBanner(text = alert, contentColor = contentColor) }
                        }
                        item {
                            GlassCard {
                                SectionTitle("未来 24 小时", contentColor)
                                TemperatureChart(
                                    hourly = weather.hourly,
                                    unit = state.settings.temperatureUnit,
                                    contentColor = contentColor
                                )
                            }
                        }
                        item {
                            GlassCard {
                                SectionTitle("逐小时", contentColor)
                                HourlyForecastRow(
                                    hourly = weather.hourly,
                                    unit = state.settings.temperatureUnit,
                                    contentColor = contentColor
                                )
                            }
                        }
                        item {
                            GlassCard {
                                SectionTitle("7 天预报", contentColor)
                                DailyForecastList(
                                    daily = weather.daily,
                                    unit = state.settings.temperatureUnit,
                                    contentColor = contentColor
                                )
                            }
                        }
                        item { FootNote(weather, contentColor) }
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = contentColor)
                }
            }

            if (!state.isLoading && weather == null) {
                ErrorState(
                    message = state.errorMessage ?: "暂无数据",
                    contentColor = contentColor,
                    onRetry = { onIntent(WeatherIntent.Retry) }
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun TopBar(
    city: City?,
    contentColor: Color,
    isLocating: Boolean,
    onLocate: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = city?.name ?: "定位中",
                color = contentColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
            val subtitle = listOfNotNull(city?.admin1, city?.country)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = contentColor.copy(alpha = 0.72f),
                    fontSize = 12.sp
                )
            }
        }
        if (isLocating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            IconButton(onClick = onLocate) {
                Icon(Icons.Filled.LocationOn, contentDescription = "定位当前位置", tint = contentColor)
            }
        }
        IconButton(onClick = onOpenSearch) {
            Icon(Icons.Filled.Search, contentDescription = "搜索城市", tint = contentColor)
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Filled.Settings, contentDescription = "设置", tint = contentColor)
        }
    }
}

@Composable
private fun LocationPromptCard(contentColor: Color, onRequest: () -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "开启定位，自动显示当地天气",
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "仅用于取经纬度，不采集、不上传",
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onRequest) {
                Text("允许", color = contentColor, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CityChips(
    cities: List<City>,
    selectedId: Long?,
    contentColor: Color,
    onSelect: (City) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cities, key = { it.id }) { city ->
            FilterChip(
                selected = city.id == selectedId,
                onClick = { onSelect(city) },
                label = { Text(city.name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.14f),
                    labelColor = contentColor,
                    selectedContainerColor = Color.White.copy(alpha = 0.32f),
                    selectedLabelColor = contentColor
                )
            )
        }
    }
}

@Composable
private fun CurrentBlock(weather: Weather, unit: TemperatureUnit, contentColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${weather.current.temperature.toDisplay(unit).toInt()}°",
                color = contentColor,
                fontSize = 76.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.width(4.dp))
            WeatherGlyphIcon(
                glyph = weatherCodeToGlyph(weather.current.weatherCode, weather.current.isDay),
                modifier = Modifier.size(72.dp),
                tint = contentColor
            )
        }
        Text(
            text = weatherCodeToLabel(weather.current.weatherCode),
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "体感 ${weather.current.apparentTemperature.toDisplay(unit).toInt()}° · " +
                "今日 ${weather.daily.firstOrNull()?.minTemperature?.toDisplay(unit)?.toInt() ?: "--"}° / " +
                "${weather.daily.firstOrNull()?.maxTemperature?.toDisplay(unit)?.toInt() ?: "--"}°",
            color = contentColor.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailItem("湿度", "${weather.current.humidity}%", contentColor, Modifier.weight(1f))
            DetailItem(
                "风速",
                String.format(Locale.CHINA, "%.1f km/h", weather.current.windSpeed),
                contentColor,
                Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailItem(
                "气压",
                "${weather.current.pressure.toInt()} hPa",
                contentColor,
                Modifier.weight(1f)
            )
            DetailItem(
                "紫外线",
                String.format(Locale.CHINA, "%.1f", weather.daily.firstOrNull()?.uvIndexMax ?: 0.0),
                contentColor,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(
            text = label,
            color = contentColor.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AlertBanner(text: String, contentColor: Color) {
    GlassCard {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionTitle(title: String, contentColor: Color) {
    Text(
        text = title,
        color = contentColor.copy(alpha = 0.85f),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun FootNote(weather: Weather, contentColor: Color) {
    val formatted = remember(weather.updatedAt) {
        SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(weather.updatedAt))
    }
    Text(
        text = "数据来源 Open-Meteo · 更新于 $formatted",
        color = contentColor.copy(alpha = 0.55f),
        fontSize = 11.sp
    )
}

@Composable
private fun ErrorState(message: String, contentColor: Color, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = contentColor,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(onClick = onRetry) { Text("重试") }
    }
}

/** 未来 12 小时降水概率达到 60% 时展示提醒，接口本身不提供灾害预警。 */
private fun alertFor(weather: Weather): String? {
    val maxProbability = weather.hourly.take(12).maxOfOrNull { it.precipitationProbability } ?: 0
    return if (maxProbability >= 60) {
        "未来 12 小时降水概率最高 $maxProbability%，出门记得带伞"
    } else {
        null
    }
}

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
