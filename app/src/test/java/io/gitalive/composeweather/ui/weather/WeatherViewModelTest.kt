package io.gitalive.composeweather.ui.weather

import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.DefaultCity
import io.gitalive.composeweather.domain.model.UserSettings
import io.gitalive.composeweather.testing.FakeSettingsRepository
import io.gitalive.composeweather.testing.FakeWeatherRepository
import io.gitalive.composeweather.testing.MainDispatcherRule
import io.gitalive.composeweather.testing.SampleCity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * 自动刷新在测试里统一关掉（autoRefreshMinutes = 0），
 * 否则无限 delay 循环会让虚拟时间无法推进到空闲。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeWeatherRepository()
    private val settingsRepository = FakeSettingsRepository()

    @Test
    fun `收藏为空时写入默认城市并加载天气`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = WeatherViewModel(repository, settingsRepository)
        advanceUntilIdle()

        assertEquals(DefaultCity.name, repository.loadedCities.single().name)
        assertNotNull(viewModel.uiState.value.weather)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.cities.size)
    }

    @Test
    fun `定位可用时优先展示当前位置`() = runTest(mainDispatcherRule.testDispatcher) {
        val located = City(
            id = 9L,
            name = "当前位置",
            latitude = 30.0,
            longitude = 120.0,
            country = "",
            isCurrentLocation = true
        )
        repository.locatedCity = located
        repository.citiesFlow.value = listOf(SampleCity)

        val viewModel = WeatherViewModel(repository, settingsRepository)
        advanceUntilIdle()

        val selected = viewModel.uiState.value.selectedCity
        assertEquals(located.id, selected?.id)
        assertTrue(selected?.isCurrentLocation == true)
        assertEquals(located.id, repository.loadedCities.last().id)
    }

    @Test
    fun `关闭自动定位时使用已选城市`() = runTest(mainDispatcherRule.testDispatcher) {
        settingsRepository.state.value = UserSettings(autoRefreshMinutes = 0, autoLocate = false)
        repository.citiesFlow.value = listOf(SampleCity)

        val viewModel = WeatherViewModel(repository, settingsRepository)
        advanceUntilIdle()

        assertEquals(SampleCity.id, viewModel.uiState.value.selectedCity?.id)
        assertEquals(SampleCity.name, repository.loadedCities.single().name)
    }

    @Test
    fun `刷新失败时保留已有数据并发出提示`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.citiesFlow.value = listOf(SampleCity)
        val viewModel = WeatherViewModel(repository, settingsRepository)
        advanceUntilIdle()

        repository.failure = IOException("offline")
        val effectDeferred = async { viewModel.effect.first() }
        viewModel.dispatch(WeatherIntent.Refresh)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.weather)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(effectDeferred.await() is WeatherEffect.ShowMessage)
    }

    @Test
    fun `首屏请求失败时展示错误态`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.citiesFlow.value = listOf(SampleCity)
        repository.failure = IOException("offline")

        val viewModel = WeatherViewModel(repository, settingsRepository)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.weather)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `切换城市会重新加载`() = runTest(mainDispatcherRule.testDispatcher) {
        val other = SampleCity.copy(id = 2L, name = "北京", latitude = 39.9042, longitude = 116.4074)
        repository.citiesFlow.value = listOf(SampleCity, other)

        val viewModel = WeatherViewModel(repository, settingsRepository)
        advanceUntilIdle()

        viewModel.dispatch(WeatherIntent.SelectCity(other))
        advanceUntilIdle()

        assertEquals("北京", repository.loadedCities.last().name)
        assertEquals(other.id, viewModel.uiState.value.selectedCity?.id)
    }
}
