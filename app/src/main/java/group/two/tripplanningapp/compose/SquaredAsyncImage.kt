package group.two.tripplanningapp.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import group.two.tripplanningapp.utilities.loadImageUrlFromFirebaseStorageUri

// TODO: add image caching

@Composable
fun SquaredAsyncImage(uri: String, size: Int) {
    val( url, setUrl) = remember{ mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = uri, block = {
        loadImageUrlFromFirebaseStorageUri(uri, setUrl, this)
    })

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color.Gray)
    ) {
        AsyncImage(
            model = url, contentDescription = "image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}