package group.two.tripplanningapp.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun RatingBar(rating: Int, onRatingChange: (Int) -> Unit) {
    Row {
        (1..5).forEach { index ->
            IconToggleButton(
                checked = index <= rating,
                onCheckedChange = { onRatingChange(index) }
            ) {
                val tint = if (index <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                Icon(Icons.Default.Star, contentDescription = null, tint = tint)
            }
        }
    }
}