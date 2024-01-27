package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.DestinationTag
import group.two.tripplanningapp.utilities.SortOption
import group.two.tripplanningapp.utilities.calculateAverageRating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: cache data and images

class DestinationsViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _storage = Firebase.storage

    var searchQuery by mutableStateOf("")
        private set

    private val _filteredDestinationData = MutableStateFlow<List<Destination>>(emptyList())
    val filteredDestinationData: StateFlow<List<Destination>> get() = _filteredDestinationData

    private val _destinationTagsData = MutableStateFlow<List<DestinationTag>>(emptyList())
    val destinationTagsData: StateFlow<List<DestinationTag>> get() = _destinationTagsData

    private val _selectedTags = mutableStateListOf<DestinationTag>()

    private var _selectedSortOption by mutableStateOf<SortOption?>(null)

    init {
        loadDestinationTagsData()
        loadDestinationData()
    }

    fun isTagSelected(tag: DestinationTag): Boolean {
        return _selectedTags.contains(tag)
    }

    fun isSelectedSortOption(sortOption: SortOption): Boolean {
        return _selectedSortOption == sortOption
    }

    fun onSortOptionChange(sortOption: SortOption) {
        _selectedSortOption = if (_selectedSortOption == sortOption) {
            null
        } else {
            sortOption
        }
        reloadDestinationDataAndApplyFiltersAndSorting()
    }

    fun toggleTag(tag: DestinationTag) {
        viewModelScope.launch {
            if (_selectedTags.contains(tag)) {
                _selectedTags.remove(tag)
            } else {
                _selectedTags.add(tag)
            }

            Log.d("tags", "Selected tags: ${_selectedTags.map { it.tagName }}")

            reloadDestinationDataAndApplyFiltersAndSorting()
        }
    }

    private fun fetchDestinationIdsFromTags(): List<String> {
        return _selectedTags.flatMap { it.destinations
        }.distinct()
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        reloadDestinationDataAndApplyFiltersAndSorting()
    }

    private fun sortDestinationsBySortOption(destinations: List<Destination>): List<Destination> {
        return when (_selectedSortOption) {
            SortOption.Name -> {
                destinations.sortedByDescending { it.name }
            }
            SortOption.Likes -> {
                destinations.sortedByDescending { it.likes }
            }
            SortOption.Rating -> {
                destinations.sortedByDescending { calculateAverageRating(it.rating) }
            }
            else -> {
                destinations.sortedBy { it.name }
            }
        }
    }

    private fun filterDestinationsBySearchQuery(destinations: List<Destination>): List<Destination> {
        return if (searchQuery.isNotBlank()) {
            destinations.filter { destination ->
                destination.name.contains(searchQuery, ignoreCase = true) ||
                        destination.location.contains(searchQuery, ignoreCase = true)
            }
        } else {
            destinations
        }
    }

    private fun reloadDestinationDataAndApplyFiltersAndSorting() {
        viewModelScope.launch {

            if(_selectedTags.isEmpty()) {
                // skip tags filter, load all destinations
                viewModelScope.launch {
                    try {
                        val querySnapshot = db.collection("destinations").get().await()

                        val destinationsList = mutableListOf<Destination>()

                        for (document in querySnapshot.documents) {
                            val destination = document.toObject(Destination::class.java)
                            destination?.let {
                                destinationsList.add(it)
                            }
                        }

                        // then sort
                        val destinationsSorted = sortDestinationsBySortOption(destinationsList)

                        // then apply search query filter
                        val destinationsFilteredBySearchQuery = filterDestinationsBySearchQuery(destinationsSorted)

                        _filteredDestinationData.value = destinationsFilteredBySearchQuery
                    } catch (e: Exception) {
                        Log.e(
                            "DestinationsViewModel",
                            "Error getting destination documents: ${e.message}",
                            e
                        )
                    }
                }
                return@launch
            }

            try {
                // apply tags filter
                val destinationIds = fetchDestinationIdsFromTags()

                if(destinationIds.isEmpty()) {
                    _filteredDestinationData.value = emptyList()
                    return@launch
                }

                val destinationsFilteredByTags = mutableListOf<Destination>()

                try {
                    val querySnapshot = db.collection("destinations")
                        .whereIn(FieldPath.documentId(), destinationIds)
                        .get()
                        .await()

                    for (document in querySnapshot.documents) {
                        val destination = document.toObject(Destination::class.java)
                        destination?.let {
                            destinationsFilteredByTags.add(it)
                        }
                    }

                    // then apply sorting
                    val destinationsSorted = sortDestinationsBySortOption(destinationsFilteredByTags)

                    // then apply search query filter
                    val destinationsFilteredBySearchQuery = filterDestinationsBySearchQuery(destinationsSorted)

                    _filteredDestinationData.value = destinationsFilteredBySearchQuery

                } catch (e: Exception) {
                    Log.e(
                        "DestinationsViewModel",
                        "Error getting destination documents: ${e.message}",
                        e
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    "DestinationsViewModel",
                    "Error getting destination documents: ${e.message}",
                    e
                )
            }
        }
    }

    private fun loadDestinationData() {
        Log.d("DestinationsViewModel", "Loading destinations data")
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("destinations").get().await()

                val destinationsList = mutableListOf<Destination>()

                for (document in querySnapshot.documents) {
                    val destination = document.toObject(Destination::class.java)
                    destination?.let {
                        destinationsList.add(it)
                    }
                }

                _filteredDestinationData.value = destinationsList
            } catch (e: Exception) {
                Log.e(
                    "DestinationsViewModel",
                    "Error getting destination documents: ${e.message}",
                    e
                )
            }
        }
    }

    private fun loadDestinationTagsData() {
        viewModelScope.launch {
            try {
                Log.d("tags", "Loading destinations tags data")

                val querySnapshot = db.collection("destinationTags").get().await()

                val destinationTags = mutableListOf<DestinationTag>()

                for (document in querySnapshot.documents) {
                    val tagName = document.getString("tagName")

                    // Concatenate the path to the "destinations" subcollection
                    val destinationsPath = "destinationTags/${document.id}/destinations"

                    // Use the concatenated path to get the "destinations" subcollection
                    val destinationsSubcollection = db.collection(destinationsPath).get().await()

                    val destinationIds = mutableListOf<String>()
                    for (destinationDoc in destinationsSubcollection.documents) {
                        val destinationId = destinationDoc.id
                        destinationId.let {
                            destinationIds.add(it)
                        }
                    }

                    val destinationTag = DestinationTag(tagName.orEmpty(), destinationIds)
                    destinationTags.add(destinationTag)
                    Log.d("tags", "Destination tag: $destinationTag")
                }

                _destinationTagsData.value = destinationTags

            } catch (e: Exception) {
                Log.e("DestinationsViewModel", "Error getting tags documents: ${e.message}", e)
            }
        }
    }

    fun loadDestinationFirstImage(path: String, setByteArray: (ByteArray) -> Unit) {
        // path example: gs://bucket/images/stars.jpg
        val imageRef = _storage.getReferenceFromUrl(path)

        val byteSize: Long = 1024 * 1024 * 5
        imageRef.getBytes(byteSize).addOnSuccessListener {
            setByteArray(it)
        }.addOnFailureListener {
            Log.e("DestinationsViewModel", "Error getting image: ${it.message}", it)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DestinationsViewModel()
            }
        }
    }
}
