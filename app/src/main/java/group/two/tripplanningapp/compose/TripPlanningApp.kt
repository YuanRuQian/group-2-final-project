package group.two.tripplanningapp.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import group.two.tripplanningapp.compose.destinationDetails.DestinationDetailsScreen
import group.two.tripplanningapp.compose.home.HomeScreen
import group.two.tripplanningapp.compose.profile.ProfileScreen
import group.two.tripplanningapp.compose.settings.SettingsScreen
import group.two.tripplanningapp.compose.trips.TripsScreen
import group.two.tripplanningapp.compose.userAuth.LoginScreen
import group.two.tripplanningapp.compose.userAuth.RegisterScreen
import group.two.tripplanningapp.viewModels.SnackbarViewModel
import group.two.tripplanningapp.viewModels.UserAuthViewModel
import kotlinx.coroutines.launch

@Composable
fun TripPlanningApp(
    userAuthViewModel: UserAuthViewModel = viewModel(factory = UserAuthViewModel.Factory),
    snackbarViewModel: SnackbarViewModel = viewModel(factory = SnackbarViewModel.Factory),
) {
    val navController = rememberNavController()
    val isLoggedIn = userAuthViewModel.isUserLoggedIn.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val (openAlertDialog, setOpenAlertDialog) = remember { mutableStateOf(false) }
    val (alertDialogMessage, setAlertDialogMessage) = remember { mutableStateOf("") }
    val( currentRoute, setCurrentRoute) = remember { mutableStateOf(Screen.Home.route) }

    fun logout() {
        userAuthViewModel.signOut(
            navigateToLoginScreen = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Login.route) {
                        inclusive = true
                    }
                }
            }
        )
    }

    fun showDialog(message: String) {
        setOpenAlertDialog(true)
        setAlertDialogMessage(message)
    }

    LaunchedEffect(snackbarViewModel) {
        launch {
            snackbarViewModel.snackbarMessage.collect { message ->
                if (message != null) {
                    snackbarHostState.showSnackbar(message)
                    snackbarViewModel.clearSnackbarMessage()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                isUserLoggedIn = isLoggedIn.value ?: false,
                logout = ::logout
            )
        },
        bottomBar = {
            if(isLoggedIn.value == true) {
                AppBottomBar(navController = navController, currentRoute = currentRoute, setCurrentRoute = setCurrentRoute)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FailureDialogHandler(
                openAlertDialog = openAlertDialog,
                setOpenAlertDialog = setOpenAlertDialog,
                alertDialogMessage = alertDialogMessage
            )
            TripPlanningNavHost(
                navController = navController,
                userAuthViewModel = userAuthViewModel,
                snackbarViewModel = snackbarViewModel,
                showDialog = ::showDialog
            )
        }
    }
}

@Composable
fun TripPlanningNavHost(
    navController: NavHostController,
    userAuthViewModel: UserAuthViewModel,
    snackbarViewModel: SnackbarViewModel,
    showDialog: (String) -> Unit
) {

    val isLoggedIn = userAuthViewModel.isUserLoggedIn.observeAsState()
    val startDestination = if (isLoggedIn.value == true) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                login = userAuthViewModel::signIn,
                navigateToRegisterScreen = {
                    navController.navigate(Screen.Register.route)
                },
                navigateToHomeScreen = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage,
                showDialog = showDialog
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                signup = userAuthViewModel::signUp,
                navigateToLoginScreen = {
                    navController.navigate(Screen.Login.route)
                },
                navigateToHomeScreen = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }
                    }
                },
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage,
                showDialog = showDialog
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onDestinationClick = {
                    navController.navigate(
                        Screen.DestinationDetails.createRoute(
                            destinationId = it,
                        ),
                    )
                },
            )
        }

        composable(route = Screen.Trips.route) {
            TripsScreen(
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
            )
        }

        composable(
            route = Screen.DestinationDetails.route,
            arguments = Screen.DestinationDetails.navArguments,
        ) {
            DestinationDetailsScreen(
                destinationId = it.arguments?.getString("destinationId") ?: ""
            )
        }
    }
}