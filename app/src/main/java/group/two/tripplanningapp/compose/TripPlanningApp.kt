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
import androidx.compose.runtime.collectAsState
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
import group.two.tripplanningapp.compose.destinationDetails.CreateNewReviewScreen
import group.two.tripplanningapp.compose.destinationDetails.DestinationDetailsScreen
import group.two.tripplanningapp.compose.home.HomeScreen
import group.two.tripplanningapp.compose.profile.ProfileScreen
import group.two.tripplanningapp.compose.settings.SettingsScreen
import group.two.tripplanningapp.compose.trips.CreateTrip
import group.two.tripplanningapp.compose.trips.TripsScreen
import group.two.tripplanningapp.compose.userAuth.LoginScreen
import group.two.tripplanningapp.compose.userAuth.RegisterScreen
import group.two.tripplanningapp.viewModels.DestinationDetailsViewModel
import group.two.tripplanningapp.viewModels.ReviewViewModel
import group.two.tripplanningapp.viewModels.SnackbarViewModel
import group.two.tripplanningapp.viewModels.UserAuthViewModel
import kotlinx.coroutines.launch

@Composable
fun TripPlanningApp(
    userAuthViewModel: UserAuthViewModel = viewModel(factory = UserAuthViewModel.Factory),
    snackbarViewModel: SnackbarViewModel = viewModel(factory = SnackbarViewModel.Factory),
    reviewViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory),
    destinationDetailsViewModel: DestinationDetailsViewModel = DestinationDetailsViewModel()
) {
    val navController = rememberNavController()
    val isLoggedIn = userAuthViewModel.isUserLoggedIn.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val (openAlertDialog, setOpenAlertDialog) = remember { mutableStateOf(false) }
    val (alertDialogMessage, setAlertDialogMessage) = remember { mutableStateOf("") }
    val (currentRoute, setCurrentRoute) = remember { mutableStateOf(Screen.Home.route) }

    fun logout() {
        reviewViewModel.clearData()
        destinationDetailsViewModel.clearData()
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
            if (isLoggedIn.value == true) {
                AppBottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    setCurrentRoute = setCurrentRoute
                )
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
                destinationDetailsViewModel = destinationDetailsViewModel,
                formatCurrency = userAuthViewModel::formatCurrency,
                formatTimestamp = userAuthViewModel::formatTimestamp,
                reviewViewModel = reviewViewModel,
                showDialog = ::showDialog,
                loadCurrentUserLocaleConstantCode = userAuthViewModel::loadCurrentUserLocaleConstantCode,
                logout = { logout() },
                setCurrentRoute = setCurrentRoute
            )
        }
    }
}

@Composable
fun TripPlanningNavHost(
    navController: NavHostController,
    userAuthViewModel: UserAuthViewModel,
    snackbarViewModel: SnackbarViewModel,
    destinationDetailsViewModel: DestinationDetailsViewModel,
    formatCurrency: (Int) -> String,
    formatTimestamp: (Long) -> String,
    reviewViewModel: ReviewViewModel,
    showDialog: (String) -> Unit,
    loadCurrentUserLocaleConstantCode: () -> Unit,
    logout: () -> Unit,
    setCurrentRoute: (String) -> Unit
) {
    val isLoggedIn = userAuthViewModel.isUserLoggedIn.observeAsState()
    val startDestination =
        if (isLoggedIn.value == true) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
    val destination = destinationDetailsViewModel.destination.collectAsState()
    val localeConstantsData = userAuthViewModel.localeConstants.collectAsState()
    val localeConstants = localeConstantsData.value
    val userTripsData = destinationDetailsViewModel.userTrips.collectAsState()
    val userTrips = userTripsData.value

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
                localeConstants = localeConstants,
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
                showDialog = showDialog,
                loadLocaleConstants = userAuthViewModel::loadLocaleData,
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                loadCurrentUserLocaleConstantCode = loadCurrentUserLocaleConstantCode,
                onDestinationClick = {
                    reviewViewModel.getDestinationReviews(it)
                    navController.navigate(
                        Screen.DestinationDetails.createRoute(
                            destinationId = it,
                        ),
                    )
                },
                setCurrentRoute = setCurrentRoute,
            )
        }

        composable(route = Screen.Trips.route) {
            TripsScreen(
                navigateToCreate = {
                    navController.navigate(Screen.CreateTrip.route)
                },
                formatCurrency = formatCurrency,
            )
        }

        composable(route = Screen.CreateTrip.route) {
            CreateTrip(
                navigateToTripsScreen = {
                    navController.navigate(Screen.Trips.route)
                }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                reviewViewModel = reviewViewModel,
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage,
                logout = logout,
                formatTimestamp = formatTimestamp
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                localeConstants = localeConstants,
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage,
                loadCurrentUserLocaleConstantCode = loadCurrentUserLocaleConstantCode
            )
        }

        composable(
            route = Screen.DestinationDetails.route,
            arguments = Screen.DestinationDetails.navArguments,
        ) {
            DestinationDetailsScreen(
                loadReviews = reviewViewModel::getDestinationReviews,
                destinationId = it.arguments?.getString("destinationId") ?: "",
                formatCurrency = formatCurrency,
                formatTimestamp = formatTimestamp,
                reviews = reviewViewModel.curDesReviews.value,
                getReviewerAvatarAndName = reviewViewModel::getReviewerAvatarAndName,
                updateReview = reviewViewModel::updateReview,
                deleteReview = reviewViewModel::deleteReview,
                loadDestinationDetails = destinationDetailsViewModel::loadDestinationDetails,
                destination = destination.value,
                createNewReview = { destinationId ->
                    navController.navigate(
                        Screen.CreateNewReview.createRoute(
                            destinationId = destinationId
                        )
                    )
                },
                loadUserTrips = destinationDetailsViewModel::loadUserTrips,
                trips = userTrips?.trips ?: emptyList(),
                addDestinationToTrip = destinationDetailsViewModel::addDestinationToTrip,
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage,
            )
        }

        composable(
            route = Screen.CreateNewReview.route,
            arguments = Screen.CreateNewReview.navArguments,
        ) {
            CreateNewReviewScreen(
                destinationId = it.arguments?.getString("destinationId") ?: "",
                createReview = reviewViewModel::addReview,
                showSnackbarMessage = snackbarViewModel::showSnackbarMessage,
                navigateBack = { navController.popBackStack() },
                loadReviewsData = reviewViewModel::getDestinationReviews,
                loadCurrentDestination = destinationDetailsViewModel::loadDestinationDetails
            )
        }
    }
}