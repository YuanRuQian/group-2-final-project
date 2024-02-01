package group.two.tripplanningapp.compose.trips

import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.viewModels.DestinationsViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreateTrip(
    navigateToTripsScreen: () -> Unit,
    destinationsViewModel: DestinationsViewModel = viewModel(factory = DestinationsViewModel.Factory)
) {
    var tripName by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf(0) }
    var privacy by remember { mutableStateOf(Privacy.Private) }
    val destinationData = destinationsViewModel.filteredDestinationData.collectAsState()
    val destinationsDes = destinationData.value
    var destinations = mutableListOf<String>()
    for (D in destinationsDes) {
        destinations.add(D.name)
    }
//    val destinations = listOf("Destination1", "Destination2", "Destination3") // Add your destination options

    var selectedDestinations by remember { mutableStateOf(mutableStateListOf("")) }
//    var selectedDestinations by remember { mutableStateOf(mutableListOf("")) }

    var existTrip = Trip("", 0, Privacy.Private, mutableListOf<String>())

    if (TripsViewModel.clickIndex != -1) {
        existTrip = TripsViewModel.trips[TripsViewModel.clickIndex]
//        Log.d("existTrip", existTrip.destinations.toString())
    }



    var tripIndex = 0
    if (existTrip.tripName != "") {
        for (i in 0 .. TripsViewModel.trips.size - 1) {
            if (existTrip.tripName == TripsViewModel.trips[i].tripName) {
                tripIndex = i
                tripName = existTrip.tripName
                numberOfPeople = existTrip.numberOfPeople
                privacy = existTrip.privacy
                while(!selectedDestinations.isEmpty()){
                    selectedDestinations.removeAt(0)
                }
                for (dd in existTrip.destinations) {
                    selectedDestinations.add(dd)
                }
                break
            }
        }
    }






    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // UI for capturing trip details
            TextField(
                value = tripName,
                onValueChange = {
                    tripName = it
                    if (TripsViewModel.clickIndex!=-1)
                    {var arr = TripsViewModel.trips.toMutableList()
                    arr[TripsViewModel.clickIndex].tripName = tripName
                    TripsViewModel.trips = arr}
                                },
                label = { Text("Trip Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = numberOfPeople.toString(),
                onValueChange = {
                    numberOfPeople = it.toIntOrNull() ?: 1
                    if (TripsViewModel.clickIndex!=-1)
                    {var arr = TripsViewModel.trips.toMutableList()
                    arr[TripsViewModel.clickIndex].numberOfPeople = numberOfPeople
                    TripsViewModel.trips = arr}
                                },
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
                        if (TripsViewModel.clickIndex!=-1)
                        {var arr = TripsViewModel.trips.toMutableList()
                        arr[TripsViewModel.clickIndex].privacy = privacy
                        TripsViewModel.trips = arr}
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

                    if (existTrip.tripName != "") {
                        var inputSelected = mutableListOf<String>()
                        for (selectedDestination in selectedDestinations) {
                            inputSelected.add(selectedDestination)
                        }
                        inputSelected.add("")
                        val newTrip = Trip(tripName, numberOfPeople, privacy, inputSelected)
                        val newTripsList: MutableList<Trip> = TripsViewModel.trips.toMutableList()
                        newTripsList[tripIndex] = newTrip
                        TripsViewModel.trips = newTripsList
                    }


                    selectedDestinations.add("") // Add an empty option
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Add Destination")
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

                if (existTrip.tripName != "") {
                    var inputSelected = mutableListOf<String>()
                    for (selectedDestination in selectedDestinations) {
                        inputSelected.add(selectedDestination)
                    }
                    inputSelected[index] = newOption
                    val newTrip = Trip(tripName, numberOfPeople, privacy, inputSelected)
                    val newTripsList: MutableList<Trip> = TripsViewModel.trips.toMutableList()
                    newTripsList[tripIndex] = newTrip
                    TripsViewModel.trips = newTripsList
                }

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
                    if (existTrip.tripName == "") {
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
                    }
                    else {
                        var inputSelected = mutableListOf<String>()
                        for (selectedDestination in selectedDestinations) {
                            inputSelected.add(selectedDestination)
                        }
                        val newTrip = Trip(tripName, numberOfPeople, privacy, inputSelected)
                        val newTripsList: MutableList<Trip> = TripsViewModel.trips.toMutableList()
                        newTripsList[tripIndex] = newTrip
                        TripsViewModel.trips = newTripsList
                        navigateToTripsScreen()

                    }

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