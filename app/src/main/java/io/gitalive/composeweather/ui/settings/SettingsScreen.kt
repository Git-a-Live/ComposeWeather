package io.gitalive.composeweather.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.ThemeMode
import io.gitalive.composeweather.widget.WeatherWidgetReceiver
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateBack -> onBack()
            }
        }
    }

    SettingsScreen(state = state, onIntent = viewModel::dispatch)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit
) {
    val settings = state.settings
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(SettingsIntent.Back) }) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SettingSection(title = "温度单位") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemperatureUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = settings.temperatureUnit == unit,
                            onClick = { onIntent(SettingsIntent.SetUnit(unit)) },
                            label = {
                                Text(if (unit == TemperatureUnit.CELSIUS) "摄氏度 °C" else "华氏度 °F")
                            }
                        )
                    }
                }
            }

            SettingSection(title = "主题") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onIntent(SettingsIntent.SetTheme(mode)) },
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> "跟随系统"
                                        ThemeMode.LIGHT -> "浅色"
                                        ThemeMode.DARK -> "深色"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            SettingSection(title = "自动刷新") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RefreshOptions.forEach { minutes ->
                        FilterChip(
                            selected = settings.autoRefreshMinutes == minutes,
                            onClick = { onIntent(SettingsIntent.SetAutoRefresh(minutes)) },
                            label = { Text(if (minutes == 0) "关闭" else "$minutes 分钟") }
                        )
                    }
                }
            }

            SettingSection(title = "定位") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "启动时自动定位",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "打开后每次进入应用都会切换到当前所在城市，需要定位权限",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.autoLocate,
                        onCheckedChange = { onIntent(SettingsIntent.SetAutoLocate(it)) }
                    )
                }
            }

            SettingSection(title = "桌面小组件") {
                Text(
                    text = "把当前天气放到桌面，支持自由拖动调整尺寸",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val requested = context.requestPinWeatherWidget()
                        if (!requested) {
                            scope.launch {
                                snackbarHostState.showSnackbar("当前桌面不支持固定小组件")
                            }
                        }
                    }
                ) {
                    Text("添加到桌面")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SettingSection(title = "关于") {
                AboutLine("数据来源", "Open-Meteo（CC BY 4.0，非商业免费额度）")
                AboutLine("架构", "MVI 单向数据流 + Kotlin 协程")
                AboutLine("依赖注入", "Hilt")
                AboutLine("本地存储", "ObjectBox 存城市与天气缓存，DataStore 存偏好")
                AboutLine("网络", "Retrofit + kotlinx.serialization + OkHttp")
            }
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun AboutLine(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val RefreshOptions = listOf(15, 30, 60, 0)

/**
 * 调起系统的固定小组件确认框。桌面不支持时返回 false，由调用方提示。
 * requestPinAppWidget 只是发请求，用户点确认后才真正落到桌面。
 */
private fun Context.requestPinWeatherWidget(): Boolean {
    val manager = AppWidgetManager.getInstance(this)
    if (!manager.isRequestPinAppWidgetSupported) return false
    val provider = ComponentName(this, WeatherWidgetReceiver::class.java)
    return manager.requestPinAppWidget(provider, null, null)
}
