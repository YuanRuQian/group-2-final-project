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

        var tripToken = ""

        var firstTime = true



        // Your logic to fetch trips from Firebase

            // Update the StateFlow with the fetched trips
//            _trips.value = firebaseTrips
        }


    fun fetchTrips() {
        viewModelScope.launch {
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
            val user = auth.currentUser
                ?: // If the user is not logged in, return
                return@launch

            Log.d("Jerry Id is??", user.uid)

            // Use Firebase API to fetch trips data
            // Replace the following code with your actual Firebase logic
//            val firebaseTripIds = firestore.collection("userProfiles").document(user?.uid ?: "").collection("trip").get().await()
//            var tripStrings = mutableListOf<String>()
//            for (document in firebaseTripIds) {
//                val tripOneId = document.toObject(TripId::class.java)
//                tripStrings.add(tripOneId.id)
//            }
//            for (everyId in tripStrings) {
//                val firebaseTrips = firestore.collection("trips").document(everyId).get().await()
//                //toObject get fields
//            //destination names another toObject
//
////                var tripName = firebaseTrips.get
//
//
//
//            }

            val documentReference = firestore.collection("userProfiles").document(user?.uid ?: "")

            val dSnapshot = documentReference.get().await()

            if(!dSnapshot.contains("trips")) {
                documentReference.update("trips", user?.uid).await()
            }



            val tripIdData = documentReference.get().await()
            var tripId = tripIdData.getString("trips")

            Log.d("Jerry tripId", tripId!!)

            val collectionReference = firestore.collection("trips")

            // 查询是否已存在相同的元素
            val querySnapshot = collectionReference.get().await()

            // 检查每个文档是否存在相同的字段名
            var haveOrNot =  querySnapshot.documents.any { document -> document.id == tripId }

            Log.d("Jerry haveOrNot", haveOrNot.toString())

            var mapTrips = mutableMapOf<String, Trip>()

//            for (index in 0 .. TripsViewModel.trips.size - 1) {
//                mapTrips.put("$index", TripsViewModel.trips[index])
//            }

//            var map = mapOf( tripId!! to  mapTrips)
            var map = mutableMapOf<String, Trip>()



            // 如果集合中不存在相同的元素，则添加新元素
            if (!haveOrNot) {
                var tripToken = ""
                tripToken = collectionReference.add(map).await().id
                documentReference.update("trips", tripToken).await()
                tripId = tripToken
            }

            TripsViewModel.tripToken = tripId


            if (TripsViewModel.firstTime) {
                val documentSnapshot = firestore.collection("trips").document(tripId).get().await()

                if (documentSnapshot.exists()) {
                    // 获取 trips 字段的值
                    val tripsMapList = documentSnapshot["trips"] as List<Map<String, Any>>?

                    // 将 List<Map<String, Any>> 转换为 List<Trip>
                    TripsViewModel.trips = tripsMapList?.map { tripMap ->
                        Trip(
                            tripName = tripMap["tripName"] as String,
                            numberOfPeople = (tripMap["numberOfPeople"] as Long).toInt(),
                            privacy = Privacy.valueOf(tripMap["privacy"] as String),
                            destinations = tripMap["destinations"] as List<String>
                        )
                    } ?: emptyList()
                }

                TripsViewModel.firstTime = false
            }
            else{
                val documentD = collectionReference.document(tripId)

                val tripsMapList = TripsViewModel.trips.map { trip ->
                    mapOf(
                        "tripName" to trip.tripName,
                        "numberOfPeople" to trip.numberOfPeople,
                        "privacy" to trip.privacy.name,
                        "destinations" to trip.destinations
                    )
                }

                documentD.set(mapOf("trips" to tripsMapList)).await()
            }







//            val documentSnapshot = collectionReference.document(tripId).get().await()
//
//            var tripMap = documentSnapshot.data?: emptyMap()
//
//            var tempTrips = mutableListOf<Trip>()
//            for (i in 0 .. tripMap.size - 1) {
//                tempTrips.add(tripMap.get("$i") as Trip)
//            }
//            TripsViewModel.trips = tempTrips

            Log.d("Jerry TripsViewModel.trips", TripsViewModel.trips.toString())


        }


    }

    }

