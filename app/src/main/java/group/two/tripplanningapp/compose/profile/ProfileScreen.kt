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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import group.two.tripplanningapp.compose.ReviewCard
import group.two.tripplanningapp.utilities.ProfileReviewSortOptions
import group.two.tripplanningapp.viewModels.ReviewViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    reviewViewModel: ReviewViewModel,
    showSnackbarMessage: (String) -> Unit,
    logout: () -> Unit,
    formatTimestamp: (Long) -> String
) {
    LaunchedEffect(key1 = true) {
        reviewViewModel.getUserReviews()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Profile(
            profileViewModel = profileViewModel,
            reviewViewModel = reviewViewModel,
            showSnackbarMessage = showSnackbarMessage,
            logout = logout,
            formatTimestamp = formatTimestamp
        )
    }
}

// Composable for the Profile screen
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    profileViewModel: ProfileViewModel,
    reviewViewModel: ReviewViewModel,
    showSnackbarMessage: (String) -> Unit,
    logout: () -> Unit,
    formatTimestamp: (Long) -> String
) {
    val profilePicLink by profileViewModel.profileImageUrl
    val userName by profileViewModel.displayName
    val userReviews by reviewViewModel.yourReviews

    var isNameEditing by remember { mutableStateOf(false) }
    var deleteAccountClicked by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    var selectedSortOption by remember { mutableStateOf(ProfileReviewSortOptions.Date) }

    val keyboardController = LocalSoftwareKeyboardController.current


    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                profileViewModel.updateProfileImage(
                    imageUri = it,
                    showSnackbarMessage = showSnackbarMessage
                )
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
                    deleteAccountClicked = true
                },
                contentPadding = PaddingValues(4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(Color.Red),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .heightIn(20.dp)
            ) {
                Text(
                    "Delete Account",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            if (deleteAccountClicked) {
                AlertDialog(
                    onDismissRequest = { deleteAccountClicked = false },
                    title = {
                        Text("WARNING")
                    },
                    text = {
                        Text("Are you sure you want to DELETE this account? All your reviews will also be deleted.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                profileViewModel.deleteAccount(
                                    reviewViewModel = reviewViewModel,
                                    showSnackbarMessage = showSnackbarMessage,
                                    logout = logout
                                )
                                deleteAccountClicked = false
                            },
                            colors = ButtonDefaults.buttonColors(Color.Red)
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                deleteAccountClicked = false
                            }
                        ) {
                            Text("No")
                        }
                    }
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



        Text(
            "Your Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.Start)
        )
        Row(
            modifier = Modifier.align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sort By: ")
            ToggleButton(
                text = "Date",
                isSelected = selectedSortOption == ProfileReviewSortOptions.Date,
                onToggle = {
                    selectedSortOption = ProfileReviewSortOptions.Date
                    reviewViewModel.profileSortReviews(ProfileReviewSortOptions.Date)
                }
            )
            ToggleButton(
                text = "Location",
                isSelected = selectedSortOption == ProfileReviewSortOptions.Location,
                onToggle = {
                    selectedSortOption = ProfileReviewSortOptions.Location
                    reviewViewModel.profileSortReviews(ProfileReviewSortOptions.Location)
                }
            )
            ToggleButton(
                text = "Rating",
                isSelected = selectedSortOption == ProfileReviewSortOptions.Rating,
                onToggle = {
                    selectedSortOption = ProfileReviewSortOptions.Rating
                    reviewViewModel.profileSortReviews(ProfileReviewSortOptions.Rating)
                }
            )
        }

        // Reviews
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(userReviews) { review ->
                ReviewCard(
                    review = review,
                    showSnackbarMessage = showSnackbarMessage,
                    showReviewCreator = true,
                    formatTimestamp = formatTimestamp,
                    getReviewerAvatarAndName = reviewViewModel::getReviewerAvatarAndName,
                    updateReview = reviewViewModel::updateReview,
                    deleteReview = reviewViewModel::deleteReview
                )
            }
            if (userReviews.isEmpty()) {
                item {
                    Text(
                        text = "No Reviews Found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
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




