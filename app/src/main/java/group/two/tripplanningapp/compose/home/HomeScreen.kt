package group.two.tripplanningapp.compose.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.utilities.byteArrayToImageBitmap
import group.two.tripplanningapp.utilities.calculateAverageRating
import group.two.tripplanningapp.viewModels.DestinationsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    destinationsViewModel: DestinationsViewModel = viewModel(factory = DestinationsViewModel.Factory),
) {
    val destinationData = destinationsViewModel.filteredDestinationData.collectAsState()
    val destinations = destinationData.value

    val destinationTagsData = destinationsViewModel.destinationTagsData.collectAsState()
    val destinationTags = destinationTagsData.value

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
                destinations = destinations,
                loadImageUrl = destinationsViewModel::loadDestinationFirstImage
            )
        }
    }
}

@Composable
fun SearchResults(
    destinations: List<Destination>,
    loadImageUrl: (path: String, setByteArray: (ByteArray) -> Unit) -> Unit
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
                    destination = destinations[index],
                    loadImageUrl = loadImageUrl
                )
            }
        )
    }
}


@Composable
fun DestinationItem(
    destination: Destination,
    loadImageUrl: (path: String, setByteArray: (ByteArray) -> Unit) -> Unit
) {
    val (byteArray, setByteArray) = remember {
        mutableStateOf<ByteArray?>(null)
    }

    LaunchedEffect(key1 = destination) {
        if (destination.imageUrls.isNotEmpty()) {
            loadImageUrl(destination.imageUrls[0]) {
                setByteArray(it)
            }
        }
    }

    @Composable
    fun renderPlaceholder() {
        Image(
            imageVector = Icons.Default.Place,
            contentDescription = "placeholder",
            modifier = Modifier.size(64.dp)
        )
    }

    val leftPercentage = 0.3f

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(leftPercentage)) {
            if (byteArray == null) {
                renderPlaceholder()
            } else {
                val imageBitmap = byteArrayToImageBitmap(byteArray)
                if (imageBitmap != null) {
                    CircularImage(imageBitmap = imageBitmap)
                } else {
                    renderPlaceholder()
                }
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


@Composable
fun CircularImage(imageBitmap: ImageBitmap) {
    val painter = BitmapPainter(imageBitmap)

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color.Gray)
    ) {
        Image(
            painter = painter,
            contentDescription = "image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}