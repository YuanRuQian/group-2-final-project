package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import group.two.tripplanningapp.compose.trips.Privacy
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.Trip
import group.two.tripplanningapp.data.TripId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class TripsViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val tripsdata: StateFlow<List<Trip>> get() = _trips

    init {
        // Initialize the ViewModel by fetching data from Firebase
        fetchTrips()
    }



    companion object {
        var trips = emptyList<Trip>()

        var clickIndex = -1



        // Your logic to fetch trips from Firebase

            // Update the StateFlow with the fetched trips
//            _trips.value = firebaseTrips
        }


    fun fetchTrips() {
        viewModelScope.launch {
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            // Use Firebase API to fetch trips data
            // Replace the following code with your actual Firebase logic
            val firebaseTripIds = firestore.collection("userProfiles").document(user?.uid ?: "").collection("trip").get().await()
            var tripStrings = mutableListOf<String>()
            for (document in firebaseTripIds) {
                val tripOneId = document.toObject(TripId::class.java)
                tripStrings.add(tripOneId.id)
            }
            for (everyId in tripStrings) {
                val firebaseTrips = firestore.collection("trips").document(everyId).get().await()
                //toObject get fields
            //destination names another toObject

//                var tripName = firebaseTrips.get



            }


        }


    }

    }

