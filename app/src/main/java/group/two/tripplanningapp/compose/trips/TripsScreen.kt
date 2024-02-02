package group.two.tripplanningapp.compose.trips

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.data.Trip
import group.two.tripplanningapp.viewModels.DestinationsViewModel
import group.two.tripplanningapp.viewModels.TripsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    navigateToCreate: () -> Unit,
    tripsViewModel: TripsViewModel = TripsViewModel()
) {

    var judge by remember { mutableStateOf<Boolean>(false)}

    LaunchedEffect(key1 = true) {
        while(true) {
            delay(2000)
            judge = TripsViewModel.tripsDataState
            if (judge) {
                break
            }
        }

    }

    if (!judge) {
        androidx.compose.material.Text(text = "Loading...")
    }

    else {

    var trips by remember { mutableStateOf(TripsViewModel.trips) }

    tripsViewModel.fetchTrips()

//    TripsViewModel.trips = getDummyTrips()


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
//                items(trips) { trip ->
//                    TripCard(trip = trip) {
//                        // Remove the trip when close button is clicked
//                        TripsViewModel.trips = TripsViewModel.trips.filter { it != trip }
//                        trips = trips.filter { it != trip }
//
//                    }
//                }
                itemsIndexed(trips) { index, trip ->
                    TripCard(index = index, navigateToCreate = navigateToCreate) {
                        // Remove the trip when close button is clicked
                        TripsViewModel.trips = TripsViewModel.trips.filterIndexed { i, _ -> i != index }
                        trips = trips.filterIndexed { i, _ -> i != index }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                // Navigate to another screen when FAB is clicked
                TripsViewModel.clickIndex = -1
                 navigateToCreate()
            },
            shape = CircleShape,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd) // 将按钮对齐到右下角
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }}

}


enum class Privacy {
    Private,
    Public
}









@Composable
fun TripCard(index : Int, destinationsViewModel: DestinationsViewModel = viewModel(factory = DestinationsViewModel.Factory), navigateToCreate: () -> Unit, onCloseClick: () -> Unit) {

    var trip by remember { mutableStateOf(Trip("",0,Privacy.Private,listOf())) }

    trip = TripsViewModel.trips[index]

    var privacy by remember { mutableStateOf(trip.privacy) }

    var Cost by remember { mutableStateOf(0) }

    val destinationData = destinationsViewModel.filteredDestinationData.collectAsState()
    val destinationsDes = destinationData.value

    Cost = 0

    for (d in trip.destinations) {
        for (Des in destinationsDes) {
            if (d == Des.name) {
                Cost = Cost + Des.averageCostPerPersonInCents*trip.numberOfPeople
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
            // Handle item click
                TripsViewModel.clickIndex = index
                Log.d("Jerry Index",index.toString())
                Log.d("Jerry clickIndex",TripsViewModel.clickIndex.toString())
                navigateToCreate()
            },
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
                    checked = privacy == Privacy.Private,
                    onCheckedChange = { isChecked ->
                        // Update privacy based on the switch state

                        privacy = if (isChecked) Privacy.Private else Privacy.Public
                        TripsViewModel.trips[index].privacy = if (isChecked) Privacy.Private else Privacy.Public
                        trip = TripsViewModel.trips[index]

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

            Text(
                text = "Cost: $Cost US Cent",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
        }
    }
}


//fun getDummyTrips(): List<Trip> {
//    return listOf(
//        Trip(
//            "Trip to Paris",
//            3,
//            Privacy.Private,
//            listOf("Eiffel Tower", "Louvre Museum", "Seine River")
//        ),
//        Trip(
//            "Summer Vacation in New York",
//            5,
//            Privacy.Public,
//            listOf("Times Square", "Central Park", "Statue of Liberty")
//        ),
//        Trip(
//            "Explore Tokyo",
//            2,
//            Privacy.Private,
//            listOf("Shibuya Crossing", "Tokyo Tower", "Asakusa Temple")
//        ),
//        Trip(
//            "Weekend Getaway to London",
//            4,
//            Privacy.Public,
//            listOf("Buckingham Palace", "British Museum", "Tower Bridge")
//        )
//    )
//}