package group.two.tripplanningapp.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.utilities.formatTimestamp
import group.two.tripplanningapp.viewModels.ReviewViewModel

@Composable
fun ReviewCard(reviewViewModel: ReviewViewModel, review: Review, showSnackbarMessage: (String) -> Unit, showReviewCreator: Boolean) {
    var isEditing by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf(review.content) }
    val fontSize = 14.sp

    var saveClicked by remember { mutableStateOf(false) }
    var deleteClicked by remember { mutableStateOf(false) }

    val reviewerAvatarURL = remember { mutableStateOf("") }
    val reviewerName = remember { mutableStateOf("") }
    reviewViewModel.getReviewerAvatarAndName(review.creatorID, reviewerAvatarURL, reviewerName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (!showReviewCreator) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // reviewer's avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = reviewerAvatarURL.value,
                            contentDescription = "Reviewer Avatar",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // reviewer's name
                    Text(
                        text = reviewerName.value,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                }
            }


            Text(
                text = review.destination,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Rating: ${review.rating}",
                fontSize = 14.sp
            )
            Text(
                text = "Created At: ${review.timeCreated?.let { formatTimestamp(it) }}",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Last Edit At: ${review.timeEdited?.let { formatTimestamp(it) }}",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))

            // Editable content text field (visible only when editing)
            if (isEditing) {
                TextField(
                    value = editedContent,
                    onValueChange = {
                        editedContent = it
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = fontSize,
                        color = Color.Black
                    ),
                )
            } else {
                // Display content text
                Text(
                    text = editedContent,
                    fontSize = fontSize
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Toggle between edit and save icon based on editing state
                IconButton(onClick = {
                    if (isEditing) {
                        // Update Content
                        saveClicked = true
                    } else {
                        // Handle edit action
                        isEditing = true
                    }
                }) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Save Review" else "Edit Review"
                    )
                }

                IconButton(onClick = {
                    if (isEditing) isEditing = false else deleteClicked = true
                }) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Cancel else Icons.Default.Delete,
                        contentDescription = if (isEditing) "Cancel Editing" else "Delete Review"
                    )
                }
            }
        }
    }

    if (saveClicked || deleteClicked) {
        AlertDialog(
            onDismissRequest = {
                // Handle dismiss if needed
                saveClicked = false
                deleteClicked = false
            },
            title = {
                Text("Confirm ${if (saveClicked) "Edit" else "Delete"}")
            },
            text = {
                Text("Are you sure you want to ${if (saveClicked) "edit" else "delete"} this review?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (saveClicked) {
                            reviewViewModel.updateReview(
                                reviewId = review.reviewId,
                                newContent = editedContent,
                                showSnackbarMessage = showSnackbarMessage,
                            )
                            saveClicked = false
                            isEditing = false
                        } else if (deleteClicked) {
                            reviewViewModel.deleteReview(
                                reviewId = review.reviewId,
                                showSnackbarMessage = showSnackbarMessage
                            )
                            deleteClicked = false
                        }
                    },
                    colors = if (saveClicked) ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(
                        Color.Red
                    )
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        saveClicked = false
                        deleteClicked = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}
