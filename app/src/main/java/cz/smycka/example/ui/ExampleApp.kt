package cz.smycka.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kiwi.orbit.compose.ui.OrbitTheme

@Composable
fun ExampleApp() {
    OrbitTheme {
        NavGraph()
    }
}

@Composable
private fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = signInNavigationRoute
    ) {
        signInScreen(onNavigateToPicture = navController::navigateToPicture)
        pictureScreen(onNavigateUp = navController::navigateUp)
    }
}
