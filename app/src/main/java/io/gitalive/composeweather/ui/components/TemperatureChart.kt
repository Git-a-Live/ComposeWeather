package io.gitalive.composeweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.gitalive.composeweather.domain.model.HourlyWeather
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.toDisplay
import io.gitalive.composeweather.ui.util.isoTimeLabel

/**
 * 未来 24 小时温度曲线。纵轴按本段数据的最小/最大值自适应，留 15% 余量防止贴边。
 */
@Composable
fun TemperatureChart(
    hourly: List<HourlyWeather>,
    unit: TemperatureUnit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    if (hourly.size < 2) return

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            val values = hourly.map { it.temperature.toDisplay(unit) }
            val minValue = values.min()
            val maxValue = values.max()
            val span = (maxValue - minValue).takeIf { it > 0.5 } ?: 1.0
            val padding = span * 0.15
            val stepX = size.width / (values.size - 1)

            val points = values.mapIndexed { index, value ->
                val ratio = ((value - minValue + padding) / (span + padding * 2)).toFloat()
                Offset(index * stepX, size.height * (1f - ratio))
            }

            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (index in 1 until points.size) {
                    val previous = points[index - 1]
                    val current = points[index]
                    val middleX = (previous.x + current.x) / 2f
                    quadraticTo(previous.x, previous.y, middleX, (previous.y + current.y) / 2f)
                }
                lineTo(points.last().x, points.last().y)
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(contentColor.copy(alpha = 0.28f), contentColor.copy(alpha = 0.02f))
                )
            )
            drawPath(
                path = linePath,
                color = contentColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            points.filterIndexed { index, _ -> index % 4 == 0 }.forEach { point ->
                drawCircle(contentColor, radius = 2.5.dp.toPx(), center = point)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(hourly.first(), hourly[hourly.size / 2], hourly.last()).forEach { item ->
                Text(
                    text = isoTimeLabel(item.time),
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "最低 ${hourly.minOf { it.temperature }.toDisplay(unit).toInt()}°",
                color = contentColor.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "最高 ${hourly.maxOf { it.temperature }.toDisplay(unit).toInt()}°",
                color = contentColor.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
