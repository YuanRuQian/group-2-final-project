package group.two.tripplanningapp.compose.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    navigateToCreate: () -> Unit
) {

    var trips by remember { mutableStateOf(emptyList<Trip>()) }

    trips = getDummyTrips()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "Your Trips",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(trips) { trip ->
                    TripCard(trip = trip) {
                        // Remove the trip when close button is clicked
                        trips = trips.filter { it != trip }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                // Navigate to another screen when FAB is clicked
                 navigateToCreate()
            },
            shape = CircleShape,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd) // 将按钮对齐到右下角
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }

}


enum class Privacy {
    Private,
    Public
}



data class Trip(
    val tripName: String,
    val numberOfPeople: Int,
    var privacy: Privacy, // Assuming Privacy is an enum class with Private and Public options
    val destinations: List<String>
)





@Composable
fun TripCard(trip: Trip, onCloseClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.tripName,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onCloseClick() },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Text(
                text = trip.tripName,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Number of People: ${trip.numberOfPeople}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy: ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = trip.privacy == Privacy.Private,
                    onCheckedChange = { isChecked ->
                        // Update privacy based on the switch state
                        trip.privacy = if (isChecked) Privacy.Private else Privacy.Public
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (trip.privacy == Privacy.Private) "Private" else "Public",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "Destinations:",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            trip.destinations.forEach { destination ->
                Text(
                    text = destination,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}


fun getDummyTrips(): List<Trip> {
    return listOf(
        Trip(
            "Trip to Paris",
            3,
            Privacy.Private,
            listOf("Eiffel Tower", "Louvre Museum", "Seine River")
        ),
        Trip(
            "Summer Vacation in New York",
            5,
            Privacy.Public,
            listOf("Times Square", "Central Park", "Statue of Liberty")
        ),
        Trip(
            "Explore Tokyo",
            2,
            Privacy.Private,
            listOf("Shibuya Crossing", "Tokyo Tower", "Asakusa Temple")
        ),
        Trip(
            "Weekend Getaway to London",
            4,
            Privacy.Public,
            listOf("Buckingham Palace", "British Museum", "Tower Bridge")
        )
    )
}