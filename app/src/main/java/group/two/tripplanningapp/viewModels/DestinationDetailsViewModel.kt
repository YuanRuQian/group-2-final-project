package group.two.tripplanningapp.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import group.two.tripplanningapp.data.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DestinationDetailsViewModel(id: String) : ViewModel() {

    private val _db = Firebase.firestore

    private val _storage = Firebase.storage

    private val _destination = MutableStateFlow<Destination?>(null)
    val destination: StateFlow<Destination?> get() = _destination

    init {
        loadDestinationDetails(id)
    }

    private fun loadDestinationDetails(id: String) {
        _db.collection("destinations").document(id).get().addOnSuccessListener {
            val destination = it.toObject(Destination::class.java)
            _destination.value = destination
        }
    }
}