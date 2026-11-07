package io.gitalive.composeweather.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import io.gitalive.composeweather.MainActivity
import io.gitalive.composeweather.di.WidgetEntryPoint
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.Weather
import io.gitalive.composeweather.domain.model.toDisplay
import io.gitalive.composeweather.ui.util.WeatherGlyph
import io.gitalive.composeweather.ui.util.paletteFor
import io.gitalive.composeweather.ui.util.weatherCodeToGlyph
import io.gitalive.composeweather.ui.util.weatherCodeToLabel
import kotlinx.coroutines.flow.first

/**
 * 桌面小组件。数据只读缓存：命中未过期缓存立即返回，过期才会触发一次网络请求。
 * 点击任意区域打开主界面。
 */
class WeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val weather = runCatching {
            entryPoint.weatherRepository().selectedCity()?.let { city ->
                entryPoint.weatherRepository().loadWeather(city, forceRefresh = false)
            }
        }.getOrNull()
        val unit = runCatching {
            entryPoint.settingsRepository().settings().first().temperatureUnit
        }.getOrDefault(TemperatureUnit.CELSIUS)

        provideContent {
            WidgetContent(weather = weather, unit = unit)
        }
    }
}

@Composable
private fun WidgetContent(weather: Weather?, unit: TemperatureUnit) {
    val size: DpSize = LocalSize.current
    val compact = size.width < 160.dp || size.height < 120.dp
    val glyph = weather?.let { weatherCodeToGlyph(it.current.weatherCode, it.current.isDay) }
        ?: WeatherGlyph.CLOUD
    val palette = paletteFor(glyph)

    val titleColor = ColorProvider(Color.White.copy(alpha = 0.82f))
    val valueColor = ColorProvider(Color.White.copy(alpha = 0.95f))
    val hintColor = ColorProvider(Color.White.copy(alpha = 0.78f))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.top))
            .cornerRadius(24.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        if (weather == null) {
            Text(
                text = "点按打开应用同步天气",
                style = TextStyle(color = hintColor, fontSize = 12.sp)
            )
            return@Column
        }

        Text(
            text = weather.city.name,
            style = TextStyle(color = titleColor, fontSize = 12.sp),
            maxLines = 1
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${weather.current.temperature.toDisplay(unit).toInt()}°",
                style = TextStyle(
                    color = valueColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            if (!compact) {
                Text(
                    text = "  ${weatherCodeToLabel(weather.current.weatherCode)}",
                    style = TextStyle(color = titleColor, fontSize = 13.sp),
                    maxLines = 1
                )
            }
        }
        Text(
            text = if (compact) {
                weatherCodeToLabel(weather.current.weatherCode)
            } else {
                val today = weather.daily.firstOrNull()
                val high = today?.maxTemperature?.toDisplay(unit)?.toInt()
                val low = today?.minTemperature?.toDisplay(unit)?.toInt()
                "最高 ${high ?: "--"}°   最低 ${low ?: "--"}°"
            },
            style = TextStyle(color = hintColor, fontSize = 11.sp),
            maxLines = 1
        )
    }
}
