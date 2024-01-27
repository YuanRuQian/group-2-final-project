package group.two.tripplanningapp.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

fun navigationWithDestinationPreCheck(
    setCurrentRoute: (String) -> Unit,
    navController: NavController,
    destination: String,
    navBackStackEntry: NavBackStackEntry?
) {
    if (navBackStackEntry?.destination?.route != destination) {
        setCurrentRoute(destination)
        navController.navigate(destination)
    }
}

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String, setCurrentRoute: (String) -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val icons = listOf(
        Icons.Default.TravelExplore,
        Icons.Default.CalendarMonth,
        Icons.Default.Person,
        Icons.Default.Settings
    )

    val routes = listOf(
        Screen.Home.route,
        Screen.Trips.route,
        Screen.Profile.route,
        Screen.Settings.route
    )

    val zippedList = icons.zip(routes)


    NavigationBar {
        zippedList.forEachIndexed { _, (icon, route) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = route) },
                label = { Text(route) },
                selected = currentRoute == route,
                onClick = {
                    navigationWithDestinationPreCheck(
                        setCurrentRoute,
                        navController,
                        route,
                        navBackStackEntry
                    )
                }
            )
        }
    }
}

