package io.gitalive.composeweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import io.gitalive.composeweather.ui.util.WeatherGlyph
import kotlin.math.cos
import kotlin.math.sin

/**
 * 纯 Canvas 绘制的天气图元，避免引入位图资源。
 * 坐标按 100x100 设计，绘制时统一缩放到实际尺寸。
 */
@Composable
fun WeatherGlyphIcon(
    glyph: WeatherGlyph,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier) {
        val factor = size.minDimension / DESIGN_SIZE
        scale(factor, factor, pivot = Offset.Zero) {
            drawGlyph(glyph, tint)
        }
    }
}

private const val DESIGN_SIZE = 100f

private fun DrawScope.drawGlyph(glyph: WeatherGlyph, tint: Color) {
    when (glyph) {
        WeatherGlyph.SUN -> drawSun(Offset(50f, 50f), 22f, tint)
        WeatherGlyph.MOON -> drawCrescentAt(Offset(50f, 50f), 26f, tint)
        WeatherGlyph.PARTLY_DAY -> {
            drawSun(Offset(63f, 36f), 15f, tint)
            drawCloud(tint, Offset.Zero)
        }
        WeatherGlyph.PARTLY_NIGHT -> {
            drawCrescentAt(Offset(63f, 36f), 16f, tint)
            drawCloud(tint, Offset.Zero)
        }
        WeatherGlyph.CLOUD -> drawCloud(tint, Offset.Zero)
        WeatherGlyph.FOG -> {
            drawCloud(tint, Offset(0f, -6f))
            drawLine(tint, Offset(26f, 84f), Offset(74f, 84f), 5f, StrokeCap.Round)
            drawLine(tint, Offset(32f, 94f), Offset(68f, 94f), 5f, StrokeCap.Round)
        }
        WeatherGlyph.DRIZZLE -> {
            drawCloud(tint, Offset(0f, -6f))
            drawLine(tint, Offset(40f, 82f), Offset(36f, 92f), 5f, StrokeCap.Round)
            drawLine(tint, Offset(60f, 82f), Offset(56f, 92f), 5f, StrokeCap.Round)
        }
        WeatherGlyph.RAIN -> {
            drawCloud(tint, Offset(0f, -6f))
            listOf(36f, 50f, 64f).forEach { x ->
                drawLine(tint, Offset(x, 80f), Offset(x - 6f, 96f), 5f, StrokeCap.Round)
            }
        }
        WeatherGlyph.SNOW -> {
            drawCloud(tint, Offset(0f, -6f))
            listOf(Offset(36f, 88f), Offset(52f, 94f), Offset(66f, 86f)).forEach {
                drawCircle(tint, 4.5f, it)
            }
        }
        WeatherGlyph.THUNDER -> {
            drawCloud(tint, Offset(0f, -8f))
            val bolt = Path().apply {
                moveTo(54f, 72f)
                lineTo(42f, 90f)
                lineTo(52f, 90f)
                lineTo(46f, 102f)
                lineTo(64f, 82f)
                lineTo(54f, 82f)
                close()
            }
            drawPath(bolt, tint)
        }
    }
}

/** 8 条射线加中心圆，半径按 100 坐标系。 */
private fun DrawScope.drawSun(center: Offset, radius: Float, tint: Color) {
    drawCircle(tint, radius * 0.62f, center)
    val inner = radius * 0.95f
    val outer = radius * 1.45f
    repeat(8) { index ->
        val angle = Math.toRadians(index * 45.0)
        val start = Offset(
            center.x + (cos(angle) * inner).toFloat(),
            center.y + (sin(angle) * inner).toFloat()
        )
        val end = Offset(
            center.x + (cos(angle) * outer).toFloat(),
            center.y + (sin(angle) * outer).toFloat()
        )
        drawLine(tint, start, end, 4f, StrokeCap.Round)
    }
}

/** 满月减去偏移圆得到月牙。 */
private fun DrawScope.drawCrescentAt(center: Offset, radius: Float, tint: Color) {
    val outer = Path().apply { addOval(Rect(center = center, radius = radius)) }
    val cut = Path().apply {
        addOval(
            Rect(
                center = Offset(center.x + radius * 0.48f, center.y - radius * 0.42f),
                radius = radius * 0.86f
            )
        )
    }
    val crescent = Path().apply { op(outer, cut, PathOperation.Difference) }
    drawPath(crescent, tint)
}

private fun DrawScope.drawCloud(tint: Color, offset: Offset) {
    val body = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(offset + Offset(20f, 56f), Size(62f, 26f)),
                radiusX = 13f,
                radiusY = 13f
            )
        )
    }
    drawPath(body, tint)
    drawCircle(tint, 15f, offset + Offset(38f, 56f))
    drawCircle(tint, 19f, offset + Offset(55f, 50f))
    drawCircle(tint, 13f, offset + Offset(70f, 58f))
}
