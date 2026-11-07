package io.gitalive.composeweather.domain.model

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val autoRefreshMinutes: Int = 30,
    val autoLocate: Boolean = true
)

/** 摄氏度按设置换算为展示值，F = C * 9 / 5 + 32。 */
fun Double.toDisplay(unit: TemperatureUnit): Double = when (unit) {
    TemperatureUnit.CELSIUS -> this
    TemperatureUnit.FAHRENHEIT -> this * 9 / 5 + 32
}

fun TemperatureUnit.symbol(): String = when (this) {
    TemperatureUnit.CELSIUS -> "°C"
    TemperatureUnit.FAHRENHEIT -> "°F"
}
