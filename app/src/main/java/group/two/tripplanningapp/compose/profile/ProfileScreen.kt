package group.two.tripplanningapp.compose.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import group.two.tripplanningapp.viewModels.ProfileViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.TextStyle
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.utilities.ProfileReviewSortOptions
import group.two.tripplanningapp.utilities.formatTimestamp

@Composable
fun ProfileScreen(
    showSnackbarMessage: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Profile(
            showSnackbarMessage = showSnackbarMessage
        )
    }
}

// Composable for the Profile screen
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    profileViewModel: ProfileViewModel = viewModel(),
    showSnackbarMessage: (String) -> Unit
) {
    val profilePicLink by profileViewModel.profileImageUrl
    val userName by profileViewModel.displayName
    val userReviews by profileViewModel.reviews


    var isNameEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }


    var selectedSortOption by remember { mutableStateOf(ProfileReviewSortOptions.Date) }

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(profileViewModel.displayName, profileViewModel.profileImageUrl) {
        profileViewModel.getProfileImage()
        profileViewModel.getDisplayName()
        profileViewModel.getUserReviews()
        editedName = userName
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                profileViewModel.updateProfileImage(
                    imageUri = it,
                    showSnackbarMessage = showSnackbarMessage)
            }
        }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
        ) {

            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .align(Alignment.Center)
                    .clickable {
                        // click to upload new profile picture
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profilePicLink,
                    contentDescription = "User Profile Image",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Delete Account Button
            Button(
                onClick = {
                    // Handle delete account action
                },
                contentPadding = PaddingValues(4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(Color.Red),
                modifier = Modifier.align(Alignment.TopEnd).heightIn(20.dp)
            ) {
                Text(
                    "Delete Account",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }



        // Display Name
        Text(
            text = userName,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        // Edit icon
        Icon(
            imageVector = if (isNameEditing) Icons.Default.Save else Icons.Default.Edit,
            contentDescription = if (isNameEditing) "Save New Display Name" else "Edit Display Name",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable {
                    if (isNameEditing) {
                        profileViewModel.updateDisplayName(
                            newDisplayName = editedName,
                            showSnackbarMessage = showSnackbarMessage
                        )
                    }
                    isNameEditing = !isNameEditing
                }
        )

        // Editable Name TextField (visible only in edit mode)
        if (isNameEditing) {
            TextField(
                value = editedName,
                onValueChange = {
                    editedName = it
                },
                label = { Text("Edit Display Name") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        isNameEditing = false
                        profileViewModel.updateDisplayName(
                            newDisplayName = editedName,
                            showSnackbarMessage = showSnackbarMessage
                        )
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                trailingIcon = {
                    // Cancel Icon
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel Name Editing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                // Cancel the editing and revert changes
                                isNameEditing = false
                                editedName = userName
                                keyboardController?.hide()
                            }
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            )
        }



        Text("Your Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier
            .padding(top = 16.dp)
            .align(Alignment.Start))
        Row(modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Sort By: ")
            ToggleButton(
                text = "Date",
                isSelected = selectedSortOption == ProfileReviewSortOptions.Date,
                onToggle = { selectedSortOption = ProfileReviewSortOptions.Date }
            )
            ToggleButton(
                text = "Location",
                isSelected = selectedSortOption == ProfileReviewSortOptions.Location,
                onToggle = { selectedSortOption = ProfileReviewSortOptions.Location }
            )
            ToggleButton(
                text = "Rating",
                isSelected = selectedSortOption == ProfileReviewSortOptions.Rating,
                onToggle = { selectedSortOption = ProfileReviewSortOptions.Rating }
            )
        }

        // Reviews
        LazyColumn {
            items(getSortedReviews(userReviews, selectedSortOption)) { review ->
                    ReviewItem(
                        profileViewModel = profileViewModel,
                        review = review,
                        showSnackbarMessage = showSnackbarMessage
                    )
            }
        }
    }
}

@Composable
fun ToggleButton(text: String, isSelected: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onToggle)
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ReviewItem(profileViewModel: ProfileViewModel, review: Review, showSnackbarMessage: (String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf(review.content) }
    val fontSize = 14.sp
    var saveClicked by remember { mutableStateOf(false) }
    var deleteClicked by remember { mutableStateOf(false) }

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
                text = "Time: ${formatTimestamp(review.timestamp)}",
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

                IconButton(onClick = {if (isEditing) isEditing=false else deleteClicked=true}) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Cancel else Icons.Default.Delete,
                        contentDescription = if (isEditing) "Cancel Editing" else "Delete Review"
                    )
                }
            }
        }
    }

    if (saveClicked||deleteClicked){
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
                        if (saveClicked){
                            profileViewModel.updateReview(
                                reviewId = review.reviewId,
                                newContent = editedContent,
                                showSnackbarMessage = showSnackbarMessage
                            )
                            saveClicked = false
                            isEditing = false
                        }
                        else if (deleteClicked){
                            profileViewModel.deleteReview(
                                reviewId = review.reviewId,
                                showSnackbarMessage = showSnackbarMessage
                            )
                            deleteClicked = false
                        }
                    }
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

// Function to get sorted reviews based on the selected sorting option
fun getSortedReviews(reviews: List<Review>, sortOption: ProfileReviewSortOptions): List<Review> {
    return when (sortOption) {
        ProfileReviewSortOptions.Date -> reviews.sortedByDescending { it.timestamp }
        ProfileReviewSortOptions.Location -> reviews.sortedBy { it.destination }
        ProfileReviewSortOptions.Rating -> reviews.sortedByDescending { it.rating }
    }
}

