package group.two.tripplanningapp.compose.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.compose.SquaredAsyncImage
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.utilities.calculateAverageRating
import group.two.tripplanningapp.viewModels.DestinationsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    loadCurrentUserLocaleConstantCode: () -> Unit,
    onDestinationClick: (String) -> Unit,
    destinationsViewModel: DestinationsViewModel = viewModel(factory = DestinationsViewModel.Factory),
) {
    val destinationData = destinationsViewModel.filteredDestinationData.collectAsState()
    val destinations = destinationData.value

    val destinationTagsData = destinationsViewModel.destinationTagsData.collectAsState()
    val destinationTags = destinationTagsData.value

    LaunchedEffect(key1 = true) {
        loadCurrentUserLocaleConstantCode()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SearchBar(
            query = destinationsViewModel.searchQuery,
            onQueryChange = destinationsViewModel::onSearchQueryChange,
            onSearch = {},
            placeholder = {
                Text(text = "Search by location or/and name")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null
                )
            },
            trailingIcon = {},
            active = true,
            onActiveChange = {},
            tonalElevation = 0.dp
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            TagsFilters(
                chips = destinationTags,
                onChipClick = destinationsViewModel::toggleTag,
                isTagSelected = destinationsViewModel::isTagSelected
            )
            SortingChips(
                onChipClick = destinationsViewModel::onSortOptionChange,
                isSortingOptionSelected = destinationsViewModel::isSelectedSortOption
            )
            SearchResults(
                onDestinationClick = onDestinationClick,
                destinations = destinations
            )
        }
    }
}

@Composable
fun SearchResults(
    onDestinationClick: (String) -> Unit,
    destinations: List<Destination>
) {
    Log.d("SearchResults", "destinations length: ${destinations.size}")

    if (destinations.isEmpty()) {
        Text(text = "No results found")
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = destinations.size,
            key = { index -> destinations[index].name },
            itemContent = { index ->
                DestinationItem(
                    onDestinationClick = onDestinationClick,
                    destination = destinations[index]
                )
            }
        )
    }
}


@Composable
fun DestinationItem(
    onDestinationClick: (String) -> Unit,
    destination: Destination
) {
    @Composable
    fun renderPlaceholder() {
        Image(
            imageVector = Icons.Default.Place,
            contentDescription = "placeholder",
            modifier = Modifier.size(64.dp)
        )
    }

    val leftPercentage = 0.3f

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
        onDestinationClick(destination.id)
    }) {
        Column(modifier = Modifier.weight(leftPercentage)) {
            if (destination.imageUrls.isEmpty()) {
                renderPlaceholder()
            } else {
                SquaredAsyncImage(uri = destination.imageUrls[0], size = 64)
            }
        }

        Column(modifier = Modifier.weight(1 - leftPercentage)) {
            Text(text = destination.name, fontWeight = FontWeight.Bold)
            Text(text = destination.location)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Rating: ${"%.2f".format(calculateAverageRating(destination.rating))}")
                Text(text = "Likes: ${destination.likes}")
            }
            // TODO: show image
        }
    }
}


