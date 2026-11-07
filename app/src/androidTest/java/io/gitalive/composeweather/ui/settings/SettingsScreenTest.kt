package io.gitalive.composeweather.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.gitalive.composeweather.domain.model.TemperatureUnit
import io.gitalive.composeweather.domain.model.ThemeMode
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.ui.theme.ComposeWeatherTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun optionsAndWidgetEntryAreDisplayed() {
        composeRule.setContent {
            ComposeWeatherTheme(dynamicColor = false) {
                SettingsScreen(
                    state = SettingsUiState(
                        settings = UserSettings(
                            temperatureUnit = TemperatureUnit.CELSIUS,
                            themeMode = ThemeMode.SYSTEM,
                            autoRefreshMinutes = 30,
                            autoLocate = true
                        )
                    ),
                    onIntent = {}
                )
            }
        }

        composeRule.onNodeWithText("温度单位").assertIsDisplayed()
        composeRule.onNodeWithText("启动时自动定位").assertIsDisplayed()
        composeRule.onNodeWithText("桌面小组件").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("添加到桌面").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clickingWidgetEntryDoesNotCrash() {
        composeRule.setContent {
            ComposeWeatherTheme(dynamicColor = false) {
                SettingsScreen(state = SettingsUiState(), onIntent = {})
            }
        }

        composeRule.onNodeWithText("添加到桌面").performScrollTo().performClick()
        composeRule.onNodeWithText("添加到桌面").assertIsDisplayed()
    }
}
