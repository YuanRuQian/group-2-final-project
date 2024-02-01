package group.two.tripplanningapp.compose.destinationDetails

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.compose.ReviewCard
import group.two.tripplanningapp.compose.SquaredAsyncImage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.Review

// TODO: fix screen flickering when navigating to this screen
@Composable
fun DestinationDetailsScreen(
    formatCurrency: (Int) -> String,
    formatTimestamp: (Long) -> String,
    destinationId: String,
    reviews: List<Review>,
    getReviewerAvatarAndName: (String, (String) -> Unit, (String) -> Unit) -> Unit,
    updateReview: (String, String, (String) -> Unit) -> Unit,
    deleteReview: (String, (String) -> Unit) -> Unit,
    loadDestinationDetails: (String) -> Unit,
    destination: Destination?
) {
    Log.d("DestinationDetailsScreen", "destinationId: $destinationId")

    LaunchedEffect(key1 = destinationId) {
        loadDestinationDetails(destinationId)
    }

    Log.d("DestinationDetailsScreen", "destination: $destination")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                deleteReview
            )
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
    deleteReview: (String, (String) -> Unit) -> Unit
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
    Text(text = "Reviews:")
    if (reviews.isEmpty()) {
        Text(text = "No reviews yet.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
        ) {
            items(count = reviews.size) { index ->
                ReviewCard(
                    review = reviews[index],
                    showSnackbarMessage = {},
                    showReviewCreator = false,
                    formatTimestamp = formatTimestamp,
                    getReviewerAvatarAndName = getReviewerAvatarAndName,
                    updateReview = updateReview,
                    deleteReview = deleteReview
                )
            }
        }
    }
}

