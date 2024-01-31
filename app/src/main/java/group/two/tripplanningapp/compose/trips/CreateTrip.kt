package group.two.tripplanningapp.compose.trips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.data.Trip
import group.two.tripplanningapp.viewModels.TripsViewModel

@Composable
fun CreateTrip(
    navigateToTripsScreen: () -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf(1) }
    var privacy by remember { mutableStateOf(Privacy.Private) }
    var selectedDestination by remember { mutableStateOf("") }
    val destinations = listOf("Destination1", "Destination2", "Destination3") // Add your destination options

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // UI for capturing trip details
        TextField(
            value = tripName,
            onValueChange = { tripName = it },
            label = { Text("Trip Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = numberOfPeople.toString(),
            onValueChange = { numberOfPeople = it.toIntOrNull() ?: 1 },
            label = { Text("Number of People") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Privacy:")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = privacy == Privacy.Private,
                onCheckedChange = { isChecked ->
                    privacy = if (isChecked) Privacy.Private else Privacy.Public
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (privacy == Privacy.Private) "Private" else "Public")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Dropdown for selecting destination
        DropdownMenu(
            expanded = !selectedDestination.isNotEmpty(),
            onDismissRequest = { selectedDestination = "" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            destinations.forEach { destination ->
                DropdownMenuItem(
                    onClick = {
                        selectedDestination = destination
                    }
                ) {
                    Text(destination)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                // Navigate back to TripsScreen
                navigateToTripsScreen()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Confirm button to add the trip to TripsScreen
        Button(
            onClick = {
                val newTrip = Trip(tripName, numberOfPeople, privacy, listOf(selectedDestination))
                // Add the new trip to the TripsScreen's list
                val newTripsList: MutableList<Trip> = TripsViewModel.trips.toMutableList()
                newTripsList.add(newTrip)
                TripsViewModel.trips = newTripsList
                // Navigate back to TripsScreen
                navigateToTripsScreen()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Confirm")
        }
    }

}