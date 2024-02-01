package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import group.two.tripplanningapp.data.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


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

    private fun fetchTrips() {
        // Use Firebase API to fetch trips data
        // Replace the following code with your actual Firebase logic
        val firebaseTrips = firestore.collection("userProfiles").document(user?.uid ?: "")
        Log.d("JERRY FETCH!!!!!",firebaseTrips.toString())
    // Your logic to fetch trips from Firebase

            // Update the StateFlow with the fetched trips
//            _trips.value = firebaseTrips
    }

    companion object {
        var trips = emptyList<Trip>()

        var clickIndex = -1

        fun fetchTrips() {
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            // Use Firebase API to fetch trips data
            // Replace the following code with your actual Firebase logic
            val firebaseTrips = firestore.collection("userProfiles").document(user?.uid ?: "")
            Log.d("JERRY FETCH!!!!!",firebaseTrips.toString())
            Log.d("Jerry User", user.toString())
            Log.d("Jerry auth", auth.toString())
            Log.d("Jerry firestore", firestore.toString())

        // Your logic to fetch trips from Firebase

            // Update the StateFlow with the fetched trips
//            _trips.value = firebaseTrips
        }
    }


}