package io.gitalive.composeweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import io.gitalive.composeweather.ui.util.WeatherGlyph
import io.gitalive.composeweather.ui.util.paletteFor

/** 整屏渐变背景，颜色随天气与昼夜变化。 */
@Composable
fun WeatherBackground(
    glyph: WeatherGlyph,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = paletteFor(glyph)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.top, palette.bottom))),
        content = content
    )
}
