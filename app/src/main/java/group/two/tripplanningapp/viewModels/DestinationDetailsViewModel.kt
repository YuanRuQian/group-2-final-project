package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import group.two.tripplanningapp.data.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DestinationDetailsViewModel: ViewModel() {

    private val _db = Firebase.firestore

    private val _storage = Firebase.storage

    private val _destination = MutableStateFlow<Destination?>(null)
    val destination: StateFlow<Destination?> get() = _destination

    fun loadDestinationDetails(id: String) {
        viewModelScope.launch {
            val res = _db.collection("destinations").document(id).get().await()
            _destination.value = res.toObject(Destination::class.java)
            Log.d("DestinationDetailsViewModel", "Destination: ${_destination.value}")
        }
    }

    private fun loadDestinationPhotoUrls(destinationUris: List<String>, urls: List<String>, setUrls: (List<String>) -> Unit) {
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
}