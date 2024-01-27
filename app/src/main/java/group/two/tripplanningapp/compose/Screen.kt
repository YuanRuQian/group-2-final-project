package group.two.tripplanningapp.compose


import androidx.navigation.NamedNavArgument

sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList(),
) {
    data object Login : Screen("Login")

    data object Register : Screen("Register")

    data object Home : Screen("Home")
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