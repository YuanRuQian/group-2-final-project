package group.two.tripplanningapp.compose.destinationDetails

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.compose.ReviewCard
import group.two.tripplanningapp.compose.SquaredAsyncImage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.viewModels.DestinationDetailsViewModel
import group.two.tripplanningapp.viewModels.ReviewViewModel

@Composable
fun DestinationDetailsScreen(
    reviewViewModel: ReviewViewModel,
    formatCurrency: (Int) -> String,
    formatTimestamp: (Long) -> String,
    destinationId: String,
    destinationDetailsViewModel: DestinationDetailsViewModel = DestinationDetailsViewModel(
        destinationId
    )
) {
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
        val destinationData = destinationDetailsViewModel.destination.collectAsState()
        val destination = destinationData.value

        if (destination == null) {
            Text(text = "Loading...")
        } else {
            DestinationDetails(destination, formatCurrency, formatTimestamp, reviewViewModel)
        }
    }
}

@Composable
fun DestinationDetails(
    destination: Destination,
    formatCurrency: (Int) -> String,
    formatTimestamp: (Long) -> String,
    reviewViewModel: ReviewViewModel
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
fun Reviews(reviews: List<Review>, viewModel: ReviewViewModel, formatTimestamp: (Long) -> String) {
    Text(text = "Reviews:")
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
    ) {
        items(count = reviews.size) { index ->
            ReviewCard(reviewViewModel = viewModel, review = reviews[index], showSnackbarMessage = {}, showReviewCreator = false, formatTimestamp = formatTimestamp)
        }
    }
}

