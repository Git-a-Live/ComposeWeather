package io.gitalive.composeweather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_FILE = "user_settings"

/** 单例 DataStore，进程内只能创建一次。 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_FILE)
