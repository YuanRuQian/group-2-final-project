package group.two.tripplanningapp.compose.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.data.Trip
import group.two.tripplanningapp.viewModels.TripsViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun CreateTrip(
//    navigateToTripsScreen: () -> Unit
//) {
//    var tripName by remember { mutableStateOf("") }
//    var numberOfPeople by remember { mutableStateOf(1) }
//    var privacy by remember { mutableStateOf(Privacy.Private) }
//    var selectedDestination by remember { mutableStateOf("") }
//    val destinations = listOf("Destination1", "Destination2", "Destination3") // Add your destination options
//
//    var selectedDestinations by remember { mutableStateOf(mutableStateListOf("")) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        // UI for capturing trip details
//        TextField(
//            value = tripName,
//            onValueChange = { tripName = it },
//            label = { Text("Trip Name") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        OutlinedTextField(
//            value = numberOfPeople.toString(),
//            onValueChange = { numberOfPeople = it.toIntOrNull() ?: 1 },
//            label = { Text("Number of People") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text("Privacy:")
//            Spacer(modifier = Modifier.width(8.dp))
//            Switch(
//                checked = privacy == Privacy.Private,
//                onCheckedChange = { isChecked ->
//                    privacy = if (isChecked) Privacy.Private else Privacy.Public
//                }
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(if (privacy == Privacy.Private) "Private" else "Public")
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//
////        DropdownMenuExample(destinations = destinations)
//
//        Button(
//            onClick = {
//                selectedDestinations.add("") // Add an empty option
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//        ) {
//            Text("Add Dropdown")
//        }
//
//        // Display DropdownMenus
//        selectedDestinations.forEachIndexed { index, selectedDestination ->
//            DropdownMenuExample(destinations = destinations, index = index, selectedOption = selectedDestination) { newOption ->
//                selectedDestinations[index] = newOption
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//        OutlinedButton(
//            onClick = {
//                // Navigate back to TripsScreen
//                navigateToTripsScreen()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//        ) {
//            Text("Back")
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        // Confirm button to add the trip to TripsScreen
//        Button(
//            onClick = {
//                val newTrip = Trip(tripName, numberOfPeople, privacy, listOf(selectedDestination))
//                // Add the new trip to the TripsScreen's list
//                val newTripsList: MutableList<Trip> = TripsViewModel.trips.toMutableList()
//                newTripsList.add(newTrip)
//                TripsViewModel.trips = newTripsList
//                // Navigate back to TripsScreen
//                navigateToTripsScreen()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//        ) {
//            Text("Confirm")
//        }
//    }
//
//}

@Composable
fun CreateTrip(
    navigateToTripsScreen: () -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf(1) }
    var privacy by remember { mutableStateOf(Privacy.Private) }
    val destinations = listOf("Destination1", "Destination2", "Destination3") // Add your destination options

    var selectedDestinations by remember { mutableStateOf(mutableStateListOf("")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
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
        }

        item {
            Button(
                onClick = {
                    selectedDestinations.add("") // Add an empty option
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Add Dropdown")
            }
        }

        // Display DropdownMenus
        items(selectedDestinations.size) { index ->
            var set = mutableSetOf<String>()
            for (s in selectedDestinations) {
                set.add(s)
            }
            var des = mutableListOf<String>()
            for (d in destinations) {
                if (!set.contains(d)) {
                    des.add(d)
                }
            }
            DropdownMenuExample(destinations = des, index = index, selectedOption = selectedDestinations[index]) { newOption ->
                selectedDestinations[index] = newOption
            }
        }

        item {
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
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Confirm button to add the trip to TripsScreen
            Button(
                onClick = {
                    var inputSelected = mutableListOf<String>()
                    for (selectedDestination in selectedDestinations) {
                        inputSelected.add(selectedDestination)
                    }
                    val newTrip = Trip(tripName, numberOfPeople, privacy, inputSelected)
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
}




@Composable
fun DropdownMenuExample(destinations: List<String>, index: Int, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }


    Box {
        // Toggle Button
        Text(
            text = "",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            color = Color.White
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .clickable { expanded = !expanded }
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = selectedOption, color = Color.White)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
        }

        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = Dp.Hairline, max = 240.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            destinations.forEach { destination ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(destination)
                        expanded = false
                    }
                ) {
                    Text(destination)
                }
            }
        }
    }
}