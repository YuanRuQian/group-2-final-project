package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.Trip
import group.two.tripplanningapp.data.Trips
import group.two.tripplanningapp.utilities.getCurrentUserID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DestinationDetailsViewModel : ViewModel() {

    private val _db = Firebase.firestore

    private val _storage = Firebase.storage

    private val _destination = MutableStateFlow<Destination?>(null)
    val destination: StateFlow<Destination?> get() = _destination

    private val _userTrips = MutableStateFlow<Trips?>(null)
    val userTrips: StateFlow<Trips?> get() = _userTrips

    fun clearData() {
        _destination.value = null
        _userTrips.value = null
    }

    fun loadUserTrips() {
        viewModelScope.launch {
            try {
                // Load user profile document
                val userProfileDocument = _db.collection("userProfiles")
                    .document(getCurrentUserID())
                    .get()
                    .await()

                // Check if the user profile document exists
                if (userProfileDocument.exists()) {
                    // Get the "trips" field which contains a list of trip IDs
                    val tripId = userProfileDocument.data?.get("trips") as? String

                    Log.d("LoadUserTrips", "Trip IDs: $tripId")

                    // Load the trips from the "trips" collection based on the IDs
                    if (!tripId.isNullOrEmpty()) {
                        _userTrips.value = _db.collection("trips")
                            .document(tripId)
                            .get()
                            .await()
                            .toObject(Trips::class.java)
                    } else {
                        // Handle the case where the user has no trips
                        // For example, update a LiveData variable with an empty list
                        _userTrips.value = null
                    }
                } else {
                    // Handle the case where the user profile document doesn't exist
                    // For example, update a LiveData variable with an empty list
                    _userTrips.value = null
                }
            } catch (e: Exception) {
                // Handle any exceptions that may occur during the process
                // For example, log the error
                Log.e("LoadUserTrips", "Error loading user trips: $e")
            }
        }
    }

    fun loadDestinationDetails(id: String) {
        viewModelScope.launch {
            val res = _db.collection("destinations").document(id).get().await()
            _destination.value = res.toObject(Destination::class.java)?.copy(id = id)
            Log.d("DestinationDetailsViewModel", "Destination: ${_destination.value}")
        }
    }

    private fun loadDestinationPhotoUrls(
        destinationUris: List<String>,
        urls: List<String>,
        setUrls: (List<String>) -> Unit
    ) {
        viewModelScope.launch {
            // path example: gs://bucket/images/stars.jpg
            destinationUris.forEach {
                val imageRef = _storage.getReferenceFromUrl(it)
                val url = imageRef.downloadUrl.await().toString()
                Log.d("DestinationsViewModel", "Image url: $url")
                setUrls(urls + url)
            }
        }
    }

    fun addDestinationToTrip(
        tripIndexInTripList: Int,
        destination: Destination,
        trip: Trip,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        Log.d("DestinationDetailsViewModel", "Adding destination to trip: $destination, tripIndexInTripList: $tripIndexInTripList, trip: $trip")
        viewModelScope.launch {
            val tripId = _db.collection("userProfiles").document(getCurrentUserID()).get().await()
                .getString("trips") ?: ""
            val newTrip = trip.copy(destinations = trip.destinations + destination.name)
            if (userTrips.value == null) {
                Log.e("DestinationDetailsViewModel", "User trips is null")
                return@launch
            }
            val newTrips = Trips(
                trips = userTrips.value?.trips?.mapIndexed { index, trip ->
                    if (index == tripIndexInTripList) newTrip else trip
                } ?: listOf()
            )
            _db.collection("trips").document(tripId).set(newTrips).addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                    _userTrips.value = newTrips
                    Log.d("DestinationDetailsViewModel", "Destination added to trip, new trips: $newTrips")
                } else {
                    onFailure()
                }
            }
        }
    }
}