package io.gitalive.composeweather.data.mapper

import io.gitalive.composeweather.data.remote.dto.CurrentDto
import io.gitalive.composeweather.data.remote.dto.DailyDto
import io.gitalive.composeweather.data.remote.dto.ForecastDto
import io.gitalive.composeweather.data.remote.dto.HourlyDto
import io.gitalive.composeweather.domain.model.City
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherMapperTest {

    private val city = City(
        name = "杭州",
        latitude = 30.2741,
        longitude = 120.1551,
        country = "中国"
    )

    @Test
    fun `current 缺失时整条报文判为无效`() {
        assertNull(baseDto().copy(current = null).toWeather(city, 0L))
    }

    @Test
    fun `daily 缺失时整条报文判为无效`() {
        assertNull(baseDto().copy(daily = null).toWeather(city, 0L))
    }

    @Test
    fun `current 关键字段缺失时判为无效`() {
        val dto = baseDto().copy(current = CurrentDto(temperature = null))
        assertNull(dto.toWeather(city, 0L))
    }

    @Test
    fun `hourly 从当前小时开始截取且最多 24 条`() {
        val weather = baseDto().toWeather(city, 1L)!!

        assertEquals(24, weather.hourly.size)
        assertEquals("2026-11-07T13:00", weather.hourly.first().time)
        assertEquals("2026-11-08T12:00", weather.hourly.last().time)
    }

    @Test
    fun `daily 字段逐个映射`() {
        val weather = baseDto().toWeather(city, 1L)!!

        assertEquals(2, weather.daily.size)
        val today = weather.daily.first()
        assertEquals("2026-11-07", today.date)
        assertEquals(19.0, today.maxTemperature, 0.001)
        assertEquals(13.0, today.minTemperature, 0.001)
        assertEquals(100, today.precipitationProbability)
        assertEquals(0.6, today.uvIndexMax, 0.001)
        assertEquals("06:23", today.sunrise)
        assertEquals("17:10", today.sunset)
    }

    @Test
    fun `current 缺省字段用兜底值`() {
        val dto = baseDto().copy(
            current = CurrentDto(
                time = "2026-11-07T13:00",
                temperature = 19.4,
                humidity = null,
                apparentTemperature = null,
                isDay = null,
                weatherCode = null,
                windSpeed = null,
                pressure = null,
                precipitation = null
            )
        )
        val current = dto.toWeather(city, 1L)!!.current

        assertEquals(19.4, current.apparentTemperature, 0.001)
        assertEquals(0, current.humidity)
        assertTrue(current.isDay)
    }

    @Test
    fun `缓存键保留四位小数`() {
        val key = City(name = "杭州", latitude = 30.27415, longitude = 120.15514, country = "中国").cacheKey()

        assertEquals("302742:1201551", key)
    }

    @Test
    fun `缓存键对负坐标同样有效`() {
        val key = City(name = "悉尼", latitude = -33.8688, longitude = 151.2093, country = "澳大利亚").cacheKey()

        assertEquals("-338688:1512093", key)
    }

    private fun baseDto(): ForecastDto {
        val hours = (13 until 43).map { offset ->
            "2026-11-%02dT%02d:00".format(7 + offset / 24, offset % 24)
        }
        return ForecastDto(
            current = CurrentDto(
                time = "2026-11-07T13:00",
                temperature = 19.4,
                humidity = 60,
                apparentTemperature = 17.2,
                isDay = 1,
                precipitation = 0.0,
                weatherCode = 3,
                pressure = 1021.0,
                windSpeed = 15.9
            ),
            hourly = HourlyDto(
                time = hours,
                temperature = hours.map { 18.0 },
                weatherCode = hours.map { 3 },
                precipitationProbability = hours.map { 20 }
            ),
            daily = DailyDto(
                time = listOf("2026-11-07", "2026-11-08"),
                weatherCode = listOf(3, 61),
                maxTemperature = listOf(19.0, 16.0),
                minTemperature = listOf(13.0, 11.0),
                precipitationProbabilityMax = listOf(100, 80),
                uvIndexMax = listOf(0.6, 0.4),
                sunrise = listOf("2026-11-07T06:23", "2026-11-08T06:24"),
                sunset = listOf("2026-11-07T17:10", "2026-11-08T17:09")
            )
        )
    }
}
