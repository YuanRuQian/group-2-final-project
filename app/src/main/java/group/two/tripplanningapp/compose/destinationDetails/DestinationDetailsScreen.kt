package group.two.tripplanningapp.compose.destinationDetails

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.compose.ReviewCard
import group.two.tripplanningapp.compose.SquaredAsyncImage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.data.Trip
import group.two.tripplanningapp.utilities.getCurrentUserID

// TODO: somtimes addDestinationToTrip fails to add the destination to the trip without any error message
@Composable
fun DestinationDetailsScreen(
    loadReviews: (String) -> Unit,
    formatCurrency: (Int) -> String,
    formatTimestamp: (Long) -> String,
    destinationId: String,
    reviews: List<Review>,
    getReviewerAvatarAndName: (String, (String) -> Unit, (String) -> Unit) -> Unit,
    updateReview: (String, String, (String) -> Unit) -> Unit,
    deleteReview: (String, (String) -> Unit) -> Unit,
    loadDestinationDetails: (String) -> Unit,
    destination: Destination?,
    createNewReview: (String) -> Unit,
    loadUserTrips: () -> Unit,
    trips: List<Trip>,
    addDestinationToTrip: (Int, Destination, Trip, ()-> Unit, ()-> Unit) -> Unit,
    showSnackbarMessage: (String) -> Unit
) {
    Log.d("DestinationDetailsScreen", "destinationId: $destinationId")

    val (canCreateNewReview, setCanCreateNewReview) = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = destinationId) {
        loadDestinationDetails(destinationId)
        loadReviews(destinationId)
        loadUserTrips()
    }

    LaunchedEffect(key1 = reviews) {
        setCanCreateNewReview(reviews.none { it.creatorID == getCurrentUserID() })
    }

    Log.d("DestinationDetailsScreen", "destination: $destination")
    Scaffold(
        floatingActionButton = {
            if (canCreateNewReview) {
                FloatingActionButton(
                    onClick = {
                        createNewReview(destinationId)
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (destination == null) {
                Text(text = "Loading...")
            } else {
                DestinationDetails(
                    destination,
                    formatCurrency,
                    formatTimestamp,
                    reviews,
                    getReviewerAvatarAndName,
                    updateReview,
                    deleteReview,
                    trips,
                    addDestinationToTrip,
                    showSnackbarMessage
                )
            }
        }
    }
}

@Composable
fun DestinationDetails(
    destination: Destination,
    formatCurrency: (Int) -> String,
    formatTimestamp: (Long) -> String,
    reviews: List<Review>,
    getReviewerAvatarAndName: (String, (String) -> Unit, (String) -> Unit) -> Unit,
    updateReview: (String, String, (String) -> Unit) -> Unit,
    deleteReview: (String, (String) -> Unit) -> Unit,
    trips: List<Trip>,
    addDestinationToTrip: (Int, Destination, Trip, ()-> Unit, ()-> Unit) -> Unit,
    showSnackbarMessage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = destination.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = destination.description,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = destination.location,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (destination.imageUrls.isNotEmpty()) {
            DestinationPhotos(destination.imageUrls)
        }

        Text(
            text = "Likes: ${destination.likes}",
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        Text(
            text = "Average Cost per Person: ${formatCurrency(destination.averageCostPerPersonInCents)}",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AddDestinationToTripDropdown(
            destination = destination,
            trips = trips,
            addDestinationToTrip = addDestinationToTrip,
            showSnackbarMessage = showSnackbarMessage
        )

        ActivitiesSummary(destination.activities)

        StarRatingStatistics(destination.rating)

        Reviews(
            reviews = reviews,
            formatTimestamp = formatTimestamp,
            getReviewerAvatarAndName = getReviewerAvatarAndName,
            updateReview = updateReview,
            deleteReview = deleteReview
        )
    }
}

@Composable
fun DestinationPhotos(imageUrls: List<String>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = imageUrls.size, key = { index -> imageUrls[index] }) { index ->
            SquaredAsyncImage(uri = imageUrls[index], size = 200)
        }
    }
}

@Composable
fun ActivitiesSummary(activities: List<String>) {
    Text(text = "Activities:")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = activities.size) { index ->
            SuggestionChip(
                label = { Text(text = activities[index]) },
                onClick = {
                    // Handle chip click if needed
                }
            )
        }
    }
}

@Composable
fun Reviews(
    reviews: List<Review>,
    formatTimestamp: (Long) -> String,
    getReviewerAvatarAndName: (String, (String) -> Unit, (String) -> Unit) -> Unit,
    updateReview: (String, String, (String) -> Unit) -> Unit,
    deleteReview: (String, (String) -> Unit) -> Unit
) {
    Log.d("Reviews", "reviews creator IDs: ${reviews.map { it.creatorID }}")
    Text(text = "Reviews:")
    if (reviews.isEmpty()) {
        Text(text = "No reviews yet.")
    } else {
        reviews.forEach { review ->
            ReviewCard(
                modifier = Modifier.padding(bottom = 8.dp),
                review = review,
                showSnackbarMessage = {},
                showReviewCreator = false,
                formatTimestamp = formatTimestamp,
                getReviewerAvatarAndName = getReviewerAvatarAndName,
                updateReview = updateReview,
                deleteReview = deleteReview,
                allowEditing = review.creatorID == getCurrentUserID()
            )
        }
    }
}

@Composable
fun AddDestinationToTripDropdown(
    trips: List<Trip>,
    destination: Destination,
    addDestinationToTrip: (Int, Destination, Trip, ()-> Unit, ()-> Unit) -> Unit,
    showSnackbarMessage: (String) -> Unit
) {
    if (trips.isEmpty()) {
        return
    }
    val (selectedTrip, setSelectedTrip) = remember { mutableStateOf(trips[0]) }
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    Text(text = "Add this destination to trip:")
    Spacer(modifier = Modifier.padding(8.dp))
    TripsDropdown(
        trips = trips,
        expanded = expanded,
        setExpanded = setExpanded,
        selectedTrip = selectedTrip,
        setSelectedTrip = setSelectedTrip,
        destination = destination,
        addDestinationToTrip = addDestinationToTrip,
        showSnackbarMessage = showSnackbarMessage
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsDropdown(
    trips: List<Trip>,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    selectedTrip: Trip,
    setSelectedTrip: (Trip) -> Unit,
    destination: Destination,
    addDestinationToTrip: (Int, Destination, Trip, () -> Unit, ()->  Unit) -> Unit,
    showSnackbarMessage: (String) -> Unit
) {

    val (isButtonEnabled, setIsButtonEnabled) = remember { mutableStateOf(selectedTrip.destinations.none { it == destination.name }) }
    Log.d("TripsDropdown", "isButtonEnabled: $isButtonEnabled, selectedTrip: $selectedTrip")

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { setExpanded(it) }) {
        CompositionLocalProvider(LocalTextInputService provides null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                TextField(
                    readOnly = true,
                    value = selectedTrip.tripName,
                    onValueChange = {},
                    label = { Text("Trip") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor()
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Button(
                    enabled = isButtonEnabled,
                    onClick = {
                        addDestinationToTrip(
                            trips.indexOf(selectedTrip),
                            destination,
                            selectedTrip,
                            {
                                showSnackbarMessage("Destination added to trip")
                                setIsButtonEnabled(false)
                            },
                            {
                                showSnackbarMessage("Failed to add destination to trip")
                            }
                        )
                    }) {
                    Text("Add")
                }
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            trips.forEach { trip ->
                DropdownMenuItem(text = {
                    Text(text = trip.tripName)
                }, onClick = {
                    setSelectedTrip(trip)
                    setIsButtonEnabled(trip.destinations.none { it == destination.name })
                    setExpanded(false)
                })
            }
        }
    }
}

