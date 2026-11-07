package io.gitalive.composeweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.gitalive.composeweather.domain.model.DailyWeather
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.toDisplay
import io.gitalive.composeweather.ui.util.dailyLabel
import io.gitalive.composeweather.ui.util.weatherCodeToGlyph

/**
 * 7 天列表。温度区间条以整段预报的最低/最高温为共同基准，便于横向比较。
 */
@Composable
fun DailyForecastList(
    daily: List<DailyWeather>,
    unit: TemperatureUnit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    if (daily.isEmpty()) return
    val overallMin = daily.minOf { it.minTemperature }
    val overallMax = daily.maxOf { it.maxTemperature }
    val span = (overallMax - overallMin).takeIf { it > 0.5 } ?: 1.0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        daily.forEach { day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dailyLabel(day.date),
                    color = contentColor,
                    fontSize = 13.sp,
                    modifier = Modifier.width(44.dp)
                )
                WeatherGlyphIcon(
                    glyph = weatherCodeToGlyph(day.weatherCode, isDay = true),
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
                Text(
                    text = if (day.precipitationProbability > 0) "${day.precipitationProbability}%" else "",
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .width(42.dp)
                        .padding(start = 6.dp)
                )
                Text(
                    text = "${day.minTemperature.toDisplay(unit).toInt()}°",
                    color = contentColor.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    modifier = Modifier.width(32.dp)
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                ) {
                    val startRatio = ((day.minTemperature - overallMin) / span).toFloat()
                    val endRatio = ((day.maxTemperature - overallMin) / span).toFloat()
                    val trackColor = contentColor.copy(alpha = 0.18f)
                    drawLine(
                        color = trackColor,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = contentColor,
                        start = androidx.compose.ui.geometry.Offset(size.width * startRatio, size.height / 2f),
                        end = androidx.compose.ui.geometry.Offset(size.width * endRatio, size.height / 2f),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                }
                Text(
                    text = "${day.maxTemperature.toDisplay(unit).toInt()}°",
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .width(38.dp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}
