package studio.forface.shuttle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import shuttle.design.theme.ShuttleTheme
import shuttle.permissions.presentation.ui.PermissionsPage
import shuttle.predictions.presentation.ui.SuggestedAppsListPage
import shuttle.settings.presentation.ui.BlacklistSettingsPage
import shuttle.settings.presentation.ui.IconPackSettingsPage
import shuttle.settings.presentation.ui.SettingsPage
import shuttle.settings.presentation.ui.WidgetLayoutPage
import studio.forface.shuttle.Destination.BlacklistSettings
import studio.forface.shuttle.Destination.IconPackSettings
import studio.forface.shuttle.Destination.Permissions
import studio.forface.shuttle.Destination.Settings
import studio.forface.shuttle.Destination.Suggestions
import studio.forface.shuttle.Destination.WidgetLayout

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShuttleTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(onFinish = this::finish)
                }
            }
        }
    }
}

@Composable
private fun App(onFinish: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Permissions) {
        composable(BlacklistSettings) { BlacklistSettingsRoute() }
        composable(IconPackSettings) { IconPackSettingsRoute() }
        composable(Permissions) { PermissionsRoute(navController) }
        composable(Settings) { SettingsRoute(navController, onFinish = onFinish) }
        composable(Suggestions) { SuggestionsRoute(navController) }
        composable(WidgetLayout) { WidgetLayoutRoute() }
    }
}

@Composable
private fun BlacklistSettingsRoute() =
    BlacklistSettingsPage()

@Composable
private fun IconPackSettingsRoute() =
    IconPackSettingsPage()

@Composable
private fun PermissionsRoute(navController: NavController) =
    PermissionsPage(toSettings = { navController.navigate(Settings, PopAll) })

@Composable
private fun SettingsRoute(navController: NavController, onFinish: () -> Unit) =
    SettingsPage(
        onBack = { navController.popOrFinish(onFinish) },
        toBlacklist = { navController.navigate(BlacklistSettings) },
        toWidgetLayout = { navController.navigate(WidgetLayout) },
        toIconPacks = { navController.navigate(IconPackSettings) },
        toPermissions = { navController.navigate(Permissions) }
    )

@Composable
private fun SuggestionsRoute(navController: NavController) =
    SuggestedAppsListPage(onSettings = { navController.navigate(Settings) })

@Composable
private fun WidgetLayoutRoute() =
    WidgetLayoutPage()

private val PopAll: NavOptions = NavOptions.Builder()
    .setPopUpTo(Permissions.id, inclusive = true)
    .build()

private fun NavController.popOrFinish(onFinish: () -> Unit) {
    if (popBackStack().not()) onFinish()
}
