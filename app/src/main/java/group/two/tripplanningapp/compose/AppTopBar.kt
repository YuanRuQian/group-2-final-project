package group.two.tripplanningapp.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    isUserLoggedIn: Boolean,
    logout: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val destination = navBackStackEntry?.destination?.route
            Text(
                text = if (isUserLoggedIn) getRouteDisplayName(destination) else "Trip Planning App by Group 2"
            )
        },
        actions = {
            if (isUserLoggedIn) {
                ExitButton(
                    logout
                )
            }
        },
    )
}

@Composable
fun ExitButton(
    logout: () -> Unit
) {
    IconButton(onClick = {
        logout()
    }) {
        Icon(
            imageVector = Icons.Filled.ExitToApp,
            contentDescription = "Log out and exit the app"
        )
    }
}
