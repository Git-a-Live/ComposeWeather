package io.gitalive.composeweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dagger.hilt.android.AndroidEntryPoint
import io.gitalive.composeweather.ui.AppViewModel
import io.gitalive.composeweather.ui.navigation.ComposeWeatherNavHost
import io.gitalive.composeweather.ui.theme.ComposeWeatherTheme
import io.gitalive.composeweather.ui.theme.resolveDark

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                    },
                null
            )
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            ComposeWeatherTheme(darkTheme = settings.themeMode.resolveDark()) {
                ComposeWeatherNavHost()
            }
        }
    }
}
