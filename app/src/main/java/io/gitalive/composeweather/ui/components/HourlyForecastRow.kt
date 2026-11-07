package io.gitalive.composeweather.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.gitalive.composeweather.domain.model.HourlyWeather
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.toDisplay
import io.gitalive.composeweather.ui.util.isoTimeLabel
import io.gitalive.composeweather.ui.util.weatherCodeToGlyph

/** 逐小时横滑。接口不返回逐小时昼夜标记，按 6-18 点视为白天。 */
@Composable
fun HourlyForecastRow(
    hourly: List<HourlyWeather>,
    unit: TemperatureUnit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(hourly) { item ->
            val isDay = item.time.substringAfter('T').take(2).toIntOrNull()?.let { it in 6..18 } ?: true
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = isoTimeLabel(item.time),
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                WeatherGlyphIcon(
                    glyph = weatherCodeToGlyph(item.weatherCode, isDay),
                    modifier = Modifier.size(30.dp),
                    tint = contentColor
                )
                Text(
                    text = "${item.temperature.toDisplay(unit).toInt()}°",
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
