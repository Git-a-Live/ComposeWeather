package io.gitalive.composeweather.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.gitalive.composeweather.data.local.entity.MyObjectBox
import io.gitalive.composeweather.data.local.settingsDataStore
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** ObjectBox 进程内只允许打开一个 Store，生命周期跟随 Application。 */
    @Provides
    @Singleton
    fun provideBoxStore(@ApplicationContext context: Context): BoxStore =
        MyObjectBox.builder()
            .androidContext(context)
            .build()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.settingsDataStore
}
