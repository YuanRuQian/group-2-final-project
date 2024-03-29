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
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.DestinationTag
import group.two.tripplanningapp.utilities.DestinationSortOption
import group.two.tripplanningapp.utilities.calculateAverageRating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: cache data and images

class DestinationsViewModel : ViewModel() {

    private val _db = Firebase.firestore

    var searchQuery by mutableStateOf("")
        private set

    private val _filteredDestinationData = MutableStateFlow<List<Destination>>(emptyList())
    val filteredDestinationData: StateFlow<List<Destination>> get() = _filteredDestinationData

    private val _destinationTagsData = MutableStateFlow<List<DestinationTag>>(emptyList())
    val destinationTagsData: StateFlow<List<DestinationTag>> get() = _destinationTagsData

    private val _selectedTags = mutableStateListOf<DestinationTag>()

    private var _selectedDestinationSortOption by mutableStateOf<DestinationSortOption?>(null)

    init {
        loadDestinationTagsData()
        loadDestinationData()
    }

    fun isTagSelected(tag: DestinationTag): Boolean {
        return _selectedTags.contains(tag)
    }

    fun isSelectedSortOption(destinationSortOption: DestinationSortOption): Boolean {
        return _selectedDestinationSortOption == destinationSortOption
    }

    fun onSortOptionChange(destinationSortOption: DestinationSortOption) {
        _selectedDestinationSortOption = if (_selectedDestinationSortOption == destinationSortOption) {
            null
        } else {
            destinationSortOption
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
        return _selectedTags.flatMap {
            it.destinations
        }.distinct()
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        reloadDestinationDataAndApplyFiltersAndSorting()
    }

    private fun sortDestinationsBySortOption(destinations: List<Destination>): List<Destination> {
        return when (_selectedDestinationSortOption) {
            DestinationSortOption.NameDesc -> {
                destinations.sortedByDescending { it.name }
            }

            DestinationSortOption.LikesDesc -> {
                destinations.sortedByDescending { it.likes }
            }

            DestinationSortOption.RatingDesc -> {
                destinations.sortedByDescending { calculateAverageRating(it.rating) }
            }

            DestinationSortOption.NameAsc -> {
                destinations.sortedBy { it.name }
            }

            DestinationSortOption.LikesAsc -> {
                destinations.sortedBy { it.likes }
            }

            DestinationSortOption.RatingAsc -> {
                destinations.sortedBy { calculateAverageRating(it.rating) }
            }

            else -> {
                destinations
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

            if (_selectedTags.isEmpty()) {
                // skip tags filter, load all destinations
                viewModelScope.launch {
                    try {
                        val querySnapshot = _db.collection("destinations").get().await()

                        val destinationsList = mutableListOf<Destination>()

                        for (document in querySnapshot.documents) {
                            val destination = document.toObject(Destination::class.java)
                            destination?.let {
                                destinationsList.add(it.copy(id = document.id))
                            }
                        }

                        // then sort
                        val destinationsSorted = sortDestinationsBySortOption(destinationsList)

                        // then apply search query filter
                        val destinationsFilteredBySearchQuery =
                            filterDestinationsBySearchQuery(destinationsSorted)

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

                if (destinationIds.isEmpty()) {
                    _filteredDestinationData.value = emptyList()
                    return@launch
                }

                val destinationsFilteredByTags = mutableListOf<Destination>()

                try {
                    val querySnapshot = _db.collection("destinations")
                        .whereIn(FieldPath.documentId(), destinationIds)
                        .get()
                        .await()

                    for (document in querySnapshot.documents) {
                        val destination = document.toObject(Destination::class.java)
                        destination?.let {
                            destinationsFilteredByTags.add(it.copy(id = document.id))
                        }
                    }

                    // then apply sorting
                    val destinationsSorted =
                        sortDestinationsBySortOption(destinationsFilteredByTags)

                    // then apply search query filter
                    val destinationsFilteredBySearchQuery =
                        filterDestinationsBySearchQuery(destinationsSorted)

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
                val querySnapshot = _db.collection("destinations").get().await()

                val destinationsList = mutableListOf<Destination>()

                for (document in querySnapshot.documents) {
                    val destination = document.toObject(Destination::class.java)
                    destination?.let {
                        destinationsList.add(it.copy(id = document.id))
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

                val querySnapshot = _db.collection("destinationTags").get().await()

                val destinationTags = mutableListOf<DestinationTag>()

                for (document in querySnapshot.documents) {
                    val tagName = document.getString("tagName")

                    // Concatenate the path to the "destinations" subcollection
                    val destinationsPath = "destinationTags/${document.id}/destinations"

                    // Use the concatenated path to get the "destinations" subcollection
                    val destinationsSubcollection = _db.collection(destinationsPath).get().await()

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DestinationsViewModel()
            }
        }
    }
}
