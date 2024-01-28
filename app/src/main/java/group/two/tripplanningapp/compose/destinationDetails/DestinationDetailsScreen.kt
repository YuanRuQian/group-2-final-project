package group.two.tripplanningapp.compose.destinationDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.viewModels.DestinationDetailsViewModel

@Composable
fun DestinationDetailsScreen(
    destinationId: String,
    destinationDetailsViewModel: DestinationDetailsViewModel = DestinationDetailsViewModel(destinationId)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val destinationData = destinationDetailsViewModel.destination.collectAsState()
        val destination = destinationData.value

        if (destination == null) {
            Text(text = "Loading...")
        } else {
            DestinationDetails(destination)
        }
    }
}

@Composable
fun DestinationDetails(destination: Destination) {
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
            text = "Average Cost per Person: $${destination.averageCostPerPersonInCents / 100.0}",
            modifier = Modifier.padding(bottom = 16.dp)
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
            AsyncImage(
                model = imageUrls[index],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}


