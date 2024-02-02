package group.two.tripplanningapp.compose


import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val displayName: String = route,
    val navArguments: List<NamedNavArgument> = emptyList(),
) {
    data object Login : Screen("Login")

    data object Register : Screen("Register")

    data object Home : Screen("Home")

    data object Trips : Screen("Trips")

    data object Profile : Screen("Profile")

    data object Settings : Screen("Settings")

    data object CreateTrip: Screen("Create")

    data object DestinationDetails : Screen(
        route = "Destination Details/{destinationId}",
        displayName = "Destination Details",
        navArguments =
        listOf(
            navArgument("destinationId") {
                type = NavType.StringType
            },
        ),
    ) {
        fun createRoute(destinationId: String) = "Destination Details/$destinationId"
    }

    data object CreateNewReview : Screen(
        route = "Create New Review/{destinationId}",
        displayName = "Create New Review",
        navArguments =
        listOf(
            navArgument("destinationId") {
                type = NavType.StringType
            },
        ),
    ) {
        fun createRoute(destinationId: String) = "Create New Review/$destinationId"
    }
}


fun getRouteDisplayName(route: String?): String {
    if (route == null) {
        return "Shopping App Demo by Lydia Yuan"
    }
    val routeSegments = route.split("/")
    return if (routeSegments.isNotEmpty()) {
        routeSegments[0]
    } else {
        route
    }
}