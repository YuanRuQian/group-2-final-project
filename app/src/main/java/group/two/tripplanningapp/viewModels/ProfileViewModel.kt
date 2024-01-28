package group.two.tripplanningapp.viewModels

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import androidx.compose.runtime.State
import java.util.UUID

// Data classes representing user profile and reviews
data class Review(val date: String, val location: String, val rating: Int, val description: String)

class ProfileViewModel : ViewModel() {
    private val TAG = "TripApppDebug"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val user = auth.currentUser


    private val _displayName = mutableStateOf("")
    val displayName: State<String>
        get() = _displayName

    private val _profileImageUrl = mutableStateOf("")
    val profileImageUrl: State<String>
        get() = _profileImageUrl


    // User Display Name
    fun getDisplayName():String{
        _displayName.value = user?.displayName ?: "Unknown User Name"
        Log.d(TAG, "getDisplayName: ${_profileImageUrl.value}")
        return displayName.value
    }
    fun updateDisplayName(newDisplayName: String) {
        user!!.updateProfile(
            userProfileChangeRequest {
                displayName = newDisplayName
            }
        ).addOnCompleteListener{
            if (it.isSuccessful){
                Log.d(TAG, "User display name updated.")
                getDisplayName()
            }
        }
    }

    // User Profile Image
    fun getProfileImage():String {
       _profileImageUrl.value = user?.photoUrl.toString()
        Log.d(TAG, "getProfileImage: ${_profileImageUrl.value}")
        return profileImageUrl.value
    }
    fun updateProfileImage(imageUri: Uri) {
        // Step 1: Upload Image to Firebase Storage
        val storageRef: StorageReference = storage.reference
        val imageFileName = "avatar_user_${auth.currentUser?.uid}.jpg"
        val imageRef: StorageReference = storageRef.child("/images/userProfiles/$imageFileName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Step 2: Get Download URL and Update User Profile
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    user!!.updateProfile(
                        userProfileChangeRequest {
                            photoUri = uri
                        }
                    ).addOnCompleteListener{
                        if (it.isSuccessful){
                            Log.d(TAG, "User profile pic updated.")
                            getProfileImage()
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting download URL: $exception")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error uploading image: $exception")
            }
    }

    fun getReview(){}

    fun updateReview(){}

    fun deleteAccount(){}

}
