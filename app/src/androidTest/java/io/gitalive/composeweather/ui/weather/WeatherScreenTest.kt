package io.gitalive.composeweather.ui.weather

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.CurrentWeather
import io.gitalive.composeweather.domain.model.DailyWeather
import io.gitalive.composeweather.domain.model.HourlyWeather
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.domain.model.Weather
import io.gitalive.composeweather.ui.theme.ComposeWeatherTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 只渲染 WeatherScreen，不启动 ViewModel，验证状态到界面的映射。
 */
@RunWith(AndroidJUnit4::class)
class WeatherScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun weatherContentIsDisplayed() {
        val weather = sampleWeather()
        val state = WeatherUiState(
            isLoading = false,
            weather = weather,
            selectedCity = weather.city,
            cities = listOf(weather.city)
        )

        composeRule.setContent {
            ComposeWeatherTheme(dynamicColor = false) {
                WeatherScreen(
                    state = state,
                    onIntent = {},
                    onOpenSearch = {},
                    onOpenSettings = {}
                )
            }
        }

        composeRule.onNodeWithText("杭州").assertIsDisplayed()
        composeRule.onNodeWithText("阴").assertIsDisplayed()
        composeRule.onNodeWithText("未来 24 小时").assertIsDisplayed()
        composeRule.onNodeWithText("7 天预报").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("19°").onFirst().assertIsDisplayed()
    }

    @Test
    fun errorStateShowsRetry() {
        val retried = AtomicBoolean(false)
        composeRule.setContent {
            ComposeWeatherTheme(dynamicColor = false) {
                WeatherScreen(
                    state = WeatherUiState(isLoading = false, weather = null, errorMessage = "网络不可用"),
                    onIntent = { intent -> retried.set(intent is WeatherIntent.Retry) },
                    onOpenSearch = {},
                    onOpenSettings = {}
                )
            }
        }

        composeRule.onNodeWithText("网络不可用").assertIsDisplayed()
        composeRule.onNodeWithText("重试").performClick()

        assertTrue(retried.get())
    }

    @Test
    fun permissionCardIsShownWhenPermissionMissing() {
        val weather = sampleWeather()
        composeRule.setContent {
            ComposeWeatherTheme(dynamicColor = false) {
                WeatherScreen(
                    state = WeatherUiState(
                        isLoading = false,
                        weather = weather,
                        selectedCity = weather.city,
                        settings = UserSettings(autoLocate = true)
                    ),
                    onIntent = {},
                    onOpenSearch = {},
                    onOpenSettings = {},
                    hasLocationPermission = false
                )
            }
        }

        composeRule.onNodeWithText("开启定位，自动显示当地天气").assertIsDisplayed()
    }

    private fun sampleWeather(): Weather {
        val city = City(
            id = 1L,
            name = "杭州",
            latitude = 30.2741,
            longitude = 120.1551,
            country = "中国",
            admin1 = "浙江省"
        )
        return Weather(
            city = city,
            current = CurrentWeather(
                temperature = 19.4,
                apparentTemperature = 17.2,
                humidity = 60,
                weatherCode = 3,
                windSpeed = 15.9,
                pressure = 1021.0,
                precipitation = 0.0,
                isDay = true
            ),
            hourly = (0 until 24).map { index ->
                HourlyWeather(
                    time = "2026-11-%02dT%02d:00".format(7 + index / 24, index % 24),
                    temperature = 18.0,
                    weatherCode = 3,
                    precipitationProbability = 20
                )
            },
            daily = (0 until 7).map { index ->
                DailyWeather(
                    date = "2026-11-%02d".format(7 + index),
                    weatherCode = 3,
                    maxTemperature = 19.0,
                    minTemperature = 13.0,
                    precipitationProbability = 100,
                    uvIndexMax = 0.6,
                    sunrise = "06:23",
                    sunset = "17:10"
                )
            },
            updatedAt = 1_700_000_000_000L
        )
    }
}
