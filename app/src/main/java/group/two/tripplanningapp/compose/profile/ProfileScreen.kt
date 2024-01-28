package group.two.tripplanningapp.compose.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import group.two.tripplanningapp.R
import group.two.tripplanningapp.viewModels.ProfileViewModel
import group.two.tripplanningapp.viewModels.Review



@Composable
fun ProfileScreen(
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //Text(text = "Profile Screen")
        Profile()
    }
}

// Composable for the Profile screen
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Profile(profileViewModel: ProfileViewModel = viewModel()) {

    val profilePicLink by profileViewModel.profileImageUrl
    val userName by profileViewModel.displayName
    val userReviews by remember { mutableStateOf(emptyList<Int>()) } // review IDs


    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(profileViewModel.displayName, profileViewModel.profileImageUrl) {
        profileViewModel.getProfileImage()
        profileViewModel.getDisplayName()
        editedName = userName
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                profileViewModel.updateProfileImage(it)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable {
                    // click to upload new profile picture
                    imagePickerLauncher.launch("image/*")
                }
        ) {
            AsyncImage(
                model = profilePicLink,
                contentDescription = "User Profile Image",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Display Name
        Row {
            Text(
                text = "Name: $userName",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            // Edit icon
            Icon(
                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                contentDescription = if (isEditing) "Save New Display Name" else "Edit Display Name",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        if (isEditing){
                            profileViewModel.updateDisplayName(editedName)
                        }
                        isEditing =!isEditing
                    }
                    .padding(8.dp)
            )
        }
        // Editable Name TextField (visible only in edit mode)
        if (isEditing) {
            Log.d("TripApppDebug", "editedName: $editedName")
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
                        isEditing = false
                        profileViewModel.updateDisplayName(editedName)
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
                                isEditing = false
                                editedName = userName
                                keyboardController?.hide()
                            }
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            )
        }



        // List of Reviews
        Text("Your Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))

//        userProfile?.reviews?.forEach { review ->
//            ReviewItem(review) { profileViewModel.deleteReview(review) }
//        }

        // Other profile management features (e.g., profile image upload, delete account) can be added here
    }
}

@Composable
fun ReviewItem(review: Review, onDeleteClick: () -> Unit) {
    // Composable for displaying individual review item
    // You can customize this based on your design and needs
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Date: ${review.date}")
        Text(text = "Location: ${review.location}")
        Text(text = "Rating: ${review.rating}")
        Text(text = "Description: ${review.description}")
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Review")
        }
    }
}