package group.two.tripplanningapp.compose.destinationDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.compose.RatingBar

// TODO: update rating info in destination
// TODO: fix user info latching issue
@Composable
fun CreateNewReviewScreen(
    destinationId: String,
    createReview: (String, String, Int, (String) -> Unit) -> Unit,
    showSnackbarMessage: (String) -> Unit,
    navigateBack: () -> Unit,
    loadReviewsData: (String) -> Unit,
    loadCurrentDestination: (String) -> Unit
) {
    val (content, setContent) = remember { mutableStateOf("") }
    val (rating, setRating) = remember { mutableStateOf(0) }
    val (enabled, setEnabled) = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Your Review:")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = content, onValueChange = {
            setContent(it)
        }, placeholder = { Text("Enter your review here...") })

        Spacer(modifier = Modifier.height(16.dp))

        RatingBar(rating = rating, onRatingChange = {
            setRating(it)
            setEnabled(it > 0)
        })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = enabled,
            onClick = {
                setEnabled(false)
                createReview(
                    destinationId,
                    content,
                    rating,
                    showSnackbarMessage
                )
                loadCurrentDestination(destinationId)
                loadReviewsData(destinationId)
                navigateBack()
            },
        ) {
            Text("Submit Review")
        }
    }
}