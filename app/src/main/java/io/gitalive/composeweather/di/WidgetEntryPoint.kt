package io.gitalive.composeweather.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.gitalive.composeweather.domain.repository.SettingsRepository
import io.gitalive.composeweather.domain.repository.WeatherRepository

/**
 * Glance 小组件跑在 BroadcastReceiver 的生命周期里，无法用 @AndroidEntryPoint 注入到
 * GlanceAppWidget 实例上，因此通过 EntryPoint 从 Application 组件取依赖。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun weatherRepository(): WeatherRepository
    fun settingsRepository(): SettingsRepository
}
