package group.two.tripplanningapp.compose.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.viewModels.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    val successMessage by settingsViewModel.successMessage.observeAsState()
    successMessage?.let {
        Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
        settingsViewModel.successMessage.value = null
    }

    val errorMessage by settingsViewModel.errorMessage.observeAsState()
    errorMessage?.let {
        Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
        settingsViewModel.errorMessage.value = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Your Feedback")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type your feedback here...") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        RatingBar(rating = rating, onRatingChange = { rating = it })

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isSubmitting = true
                settingsViewModel.submitFeedback(feedbackText, rating)
                feedbackText = ""
                rating = 0 // Reset rating
                isSubmitting = false
            },
            enabled = !isSubmitting
        ) {
            Text("Submit Feedback")
        }
    }
}

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