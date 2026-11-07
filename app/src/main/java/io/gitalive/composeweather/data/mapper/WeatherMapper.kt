package io.gitalive.composeweather.data.mapper

import io.gitalive.composeweather.data.remote.dto.ForecastDto
import io.gitalive.composeweather.domain.model.City
import io.gitalive.composeweather.domain.model.CurrentWeather
import io.gitalive.composeweather.domain.model.DailyWeather
import io.gitalive.composeweather.domain.model.HourlyWeather
import io.gitalive.composeweather.domain.model.Weather
import kotlin.math.roundToLong

/**
 * DTO 到领域模型。current 或 daily 缺失时返回 null，由仓库层判定为无效报文。
 * 小时数据按当前小时截取，最多 24 条，避免把当天已过去的小时也展示出来。
 */
fun ForecastDto.toWeather(city: City, updatedAt: Long): Weather? {
    val currentDto = current ?: return null
    val dailyDto = daily ?: return null

    val current = CurrentWeather(
        temperature = currentDto.temperature ?: return null,
        apparentTemperature = currentDto.apparentTemperature ?: currentDto.temperature,
        humidity = currentDto.humidity ?: 0,
        weatherCode = currentDto.weatherCode ?: 0,
        windSpeed = currentDto.windSpeed ?: 0.0,
        pressure = currentDto.pressure ?: 0.0,
        precipitation = currentDto.precipitation ?: 0.0,
        isDay = (currentDto.isDay ?: 1) == 1
    )

    val hourlyDto = hourly
    val hourly = buildList {
        val times = hourlyDto?.time.orEmpty()
        times.indices.forEach { index ->
            val dto = hourlyDto ?: return@forEach
            val temperature = dto.temperature.getOrNull(index) ?: return@forEach
            add(
                HourlyWeather(
                    time = times[index],
                    temperature = temperature,
                    weatherCode = dto.weatherCode.getOrNull(index) ?: 0,
                    precipitationProbability = dto.precipitationProbability.getOrNull(index) ?: 0
                )
            )
        }
    }.let { list ->
        val currentHour = currentDto.time?.substringBefore(':') ?: return@let list.take(HOURLY_LIMIT)
        val upcoming = list.filter { it.time.substringBefore(':') >= currentHour }
        (if (upcoming.isEmpty()) list else upcoming).take(HOURLY_LIMIT)
    }

    val daily = buildList {
        dailyDto.time.indices.forEach { index ->
            val max = dailyDto.maxTemperature.getOrNull(index) ?: return@forEach
            val min = dailyDto.minTemperature.getOrNull(index) ?: return@forEach
            add(
                DailyWeather(
                    date = dailyDto.time[index],
                    weatherCode = dailyDto.weatherCode.getOrNull(index) ?: 0,
                    maxTemperature = max,
                    minTemperature = min,
                    precipitationProbability = dailyDto.precipitationProbabilityMax.getOrNull(index) ?: 0,
                    uvIndexMax = dailyDto.uvIndexMax.getOrNull(index) ?: 0.0,
                    sunrise = dailyDto.sunrise.getOrNull(index)?.substringAfter('T').orEmpty(),
                    sunset = dailyDto.sunset.getOrNull(index)?.substringAfter('T').orEmpty()
                )
            )
        }
    }

    return Weather(
        city = city,
        current = current,
        hourly = hourly,
        daily = daily,
        updatedAt = updatedAt
    )
}

/** 缓存键保留 4 位小数，与接口精度一致。 */
fun City.cacheKey(): String {
    val latitude = (latitude * CACHE_PRECISION).roundToLong()
    val longitude = (longitude * CACHE_PRECISION).roundToLong()
    return "$latitude:$longitude"
}

private const val HOURLY_LIMIT = 24
private const val CACHE_PRECISION = 10_000.0
