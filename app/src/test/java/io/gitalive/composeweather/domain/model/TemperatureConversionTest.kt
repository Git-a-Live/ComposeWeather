package io.gitalive.composeweather.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureConversionTest {

    @Test
    fun `摄氏转华氏`() {
        assertEquals(32.0, 0.0.toDisplay(TemperatureUnit.FAHRENHEIT), 0.001)
        assertEquals(212.0, 100.0.toDisplay(TemperatureUnit.FAHRENHEIT), 0.001)
    }

    @Test
    fun `负四十度两种单位相等`() {
        assertEquals(-40.0, (-40.0).toDisplay(TemperatureUnit.FAHRENHEIT), 0.001)
    }

    @Test
    fun `摄氏不做换算`() {
        assertEquals(19.4, 19.4.toDisplay(TemperatureUnit.CELSIUS), 0.0)
    }

    @Test
    fun `单位符号`() {
        assertEquals("°C", TemperatureUnit.CELSIUS.symbol())
        assertEquals("°F", TemperatureUnit.FAHRENHEIT.symbol())
    }
}
