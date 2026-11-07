package io.gitalive.composeweather.ui.util

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** "2026-11-07" 转"今天/明天/周X"，解析失败时原样返回。 */
fun dailyLabel(date: String): String {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return date
    val today = LocalDate.now()
    return when (parsed) {
        today -> "今天"
        today.plusDays(1) -> "明天"
        else -> parsed.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
    }
}

/** ISO 时间串 "2026-11-07T13:00" 取 "13:00"。 */
fun isoTimeLabel(time: String): String = time.substringAfter('T', time).take(5)

fun temperatureText(value: Double): String = "${value.toInt()}°"
