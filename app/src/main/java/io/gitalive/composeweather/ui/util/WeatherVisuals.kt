package io.gitalive.composeweather.ui.util

import androidx.compose.ui.graphics.Color

/** 天气图元类型，Canvas 绘制与背景配色都按它分支。 */
enum class WeatherGlyph {
    SUN,
    MOON,
    PARTLY_DAY,
    PARTLY_NIGHT,
    CLOUD,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDER
}

/** WMO 天气代码到图元。代码表见 Open-Meteo 文档的 Weather variable documentation。 */
fun weatherCodeToGlyph(code: Int, isDay: Boolean): WeatherGlyph = when (code) {
    0 -> if (isDay) WeatherGlyph.SUN else WeatherGlyph.MOON
    1, 2 -> if (isDay) WeatherGlyph.PARTLY_DAY else WeatherGlyph.PARTLY_NIGHT
    3 -> WeatherGlyph.CLOUD
    45, 48 -> WeatherGlyph.FOG
    51, 53, 55, 56, 57 -> WeatherGlyph.DRIZZLE
    61, 63, 65, 66, 67, 80, 81, 82 -> WeatherGlyph.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherGlyph.SNOW
    95, 96, 99 -> WeatherGlyph.THUNDER
    else -> WeatherGlyph.CLOUD
}

fun weatherCodeToLabel(code: Int): String = when (code) {
    0 -> "晴"
    1 -> "大部晴朗"
    2 -> "局部多云"
    3 -> "阴"
    45, 48 -> "雾"
    51, 53, 55 -> "毛毛雨"
    56, 57 -> "冻毛毛雨"
    61 -> "小雨"
    63 -> "中雨"
    65 -> "大雨"
    66, 67 -> "冻雨"
    71 -> "小雪"
    73 -> "中雪"
    75 -> "大雪"
    77 -> "米雪"
    80, 81 -> "阵雨"
    82 -> "强阵雨"
    85, 86 -> "阵雪"
    95 -> "雷阵雨"
    96, 99 -> "雷暴伴冰雹"
    else -> "未知"
}

data class WeatherPalette(val top: Color, val bottom: Color, val content: Color)

private val White = Color(0xFFF7FAFF)

/** 背景渐变。夜间统一压暗，降水类偏灰蓝，雷暴偏紫。 */
fun paletteFor(glyph: WeatherGlyph): WeatherPalette = when (glyph) {
    WeatherGlyph.SUN -> WeatherPalette(Color(0xFF3D8BD4), Color(0xFF9CC9E8), White)
    WeatherGlyph.PARTLY_DAY -> WeatherPalette(Color(0xFF4C86BE), Color(0xFFA7C4D9), White)
    WeatherGlyph.MOON -> WeatherPalette(Color(0xFF16233D), Color(0xFF3B4E75), White)
    WeatherGlyph.PARTLY_NIGHT -> WeatherPalette(Color(0xFF1B2946), Color(0xFF44567D), White)
    WeatherGlyph.CLOUD -> WeatherPalette(Color(0xFF4A5866), Color(0xFF8593A1), White)
    WeatherGlyph.FOG -> WeatherPalette(Color(0xFF5A6470), Color(0xFF9AA4AE), White)
    WeatherGlyph.DRIZZLE -> WeatherPalette(Color(0xFF3F5566), Color(0xFF7C93A5), White)
    WeatherGlyph.RAIN -> WeatherPalette(Color(0xFF324A5E), Color(0xFF6E8698), White)
    WeatherGlyph.SNOW -> WeatherPalette(Color(0xFF5C7793), Color(0xFFA9C0D4), White)
    WeatherGlyph.THUNDER -> WeatherPalette(Color(0xFF27263A), Color(0xFF565273), White)
}
