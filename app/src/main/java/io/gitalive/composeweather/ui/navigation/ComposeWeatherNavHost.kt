package io.gitalive.composeweather.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.gitalive.composeweather.ui.search.SearchRoute
import io.gitalive.composeweather.ui.settings.SettingsRoute
import io.gitalive.composeweather.ui.weather.WeatherRoute

sealed class Route(val path: String) {
    data object Weather : Route("weather")
    data object Search : Route("search")
    data object Settings : Route("settings")
}

@Composable
fun ComposeWeatherNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Weather.path) {
        composable(Route.Weather.path) {
            WeatherRoute(
                onOpenSearch = { navController.navigate(Route.Search.path) },
                onOpenSettings = { navController.navigate(Route.Settings.path) }
            )
        }
        composable(Route.Search.path) {
            SearchRoute(onBack = { navController.popBackStack() })
        }
        composable(Route.Settings.path) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
