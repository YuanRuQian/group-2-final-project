package group.two.tripplanningapp.viewModels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import androidx.compose.runtime.State
import com.google.firebase.firestore.FirebaseFirestore


class ProfileViewModel : ViewModel() {

    private val TAG = "TripApppDebug_ProfileViewModel"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _displayName = mutableStateOf("")
    val displayName: State<String> get() = _displayName

    private val _profileImageUrl = mutableStateOf("")
    val profileImageUrl: State<String> get() = _profileImageUrl

    init {
        getProfileImage()
        getDisplayName()
    }

    // User Display Name
    private fun getDisplayName(){
        _displayName.value = user?.displayName ?: "Unknown User Name"
        Log.d(TAG, "getDisplayName: ${displayName.value}")
    }
    fun updateDisplayName(newDisplayName: String, showSnackbarMessage: (String) -> Unit) {

        // Step 1: Update name in User Auth
        user!!.updateProfile(
            userProfileChangeRequest { displayName = newDisplayName }
        ).addOnSuccessListener{

            // Step 2: Update name in database userProfile
            val profileDocumentRef = firestore.collection("userProfiles").document(user?.uid?:"")
            profileDocumentRef.update(hashMapOf("userName" to newDisplayName).toMap()).addOnSuccessListener {

                // Step 3: Success
                Log.d(TAG, "User name updated.")
                showSnackbarMessage("User name updated.")
                getDisplayName()

            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error update database userProfile userName: $exception")
                showSnackbarMessage("Error update database userProfile userName")
            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error update auth userName: $exception")
            showSnackbarMessage("Error update auth userName")
        }
    }

    // User Profile Image
    private fun getProfileImage(){
        // get Avatar url from auth
        _profileImageUrl.value = user?.photoUrl.toString()

        // if auth avatar doesn't exist, load default avatar url
        if (profileImageUrl.value == "null") {
            val defaultAvatarUri = "/images/userProfiles/avatar_default.webp"
            val imageRef: StorageReference = storage.reference.child(defaultAvatarUri)
            imageRef.downloadUrl.addOnSuccessListener{uri ->
                _profileImageUrl.value = uri.toString()
                Log.d(TAG, "getProfileImage: ${_profileImageUrl.value}")
            }
        }
        Log.d(TAG, "getProfileImage: ${_profileImageUrl.value}")
    }
    fun updateProfileImage(imageUri: Uri, showSnackbarMessage: (String) -> Unit) {
        val storageRef: StorageReference = storage.reference
        val imageFileName = "avatar_user_${auth.currentUser?.uid}.jpg"
        val imageRef: StorageReference = storageRef.child("/images/userProfiles/$imageFileName")


        // Step 1: Upload Image to Firebase Storage
        imageRef.putFile(imageUri).addOnSuccessListener {

            // Step 2: Get Download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->

                // Step 3:  Update User Auth Photo URI
                user!!.updateProfile(userProfileChangeRequest { photoUri = uri }).addOnSuccessListener{

                    // Step 4: Update database userProfile photo URI
                    val profileDocumentRef = firestore.collection("userProfiles").document(user?.uid?:"")
                    profileDocumentRef.update(hashMapOf("avatar" to uri.toString()).toMap()).addOnSuccessListener {

                        // Step 5: Success
                        Log.d(TAG, "User profile pic updated.")
                        showSnackbarMessage("User profile picture updated.")
                        getProfileImage()

                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Update database userProfile photo URI: $exception")
                        showSnackbarMessage("Error update database userProfile photo URI.")
                    }

                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error update User Profile Photo URI: $exception")
                }

            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting download URL: $exception")
            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error uploading image: $exception")
        }
    }

    // Delete Account
    fun deleteAccount(reviewViewModel: ReviewViewModel, showSnackbarMessage: (String) -> Unit, logout:()->Unit){

        // Step 1: Delete all reviews by the user in database
        reviewViewModel.yourReviews.value.forEach{ eachReview ->
            reviewViewModel.deleteReview(eachReview.reviewId, showSnackbarMessage = {Log.e(TAG, "Delete Review: $eachReview")})
        }

        // Step 2: Delete user profile in data base
        firestore.collection("userProfiles").document(user?.uid?:"")
            .delete().addOnSuccessListener{
                Log.d(TAG, "ASuccessful delete userProfile in data base")
            }.addOnFailureListener{ exception ->
                Log.e(TAG, "Error delete userProfile in data base: $exception")
            }

        // Step 3: Delete user avatar image file in storage
        val avatarRef: StorageReference = storage.reference.child("/images/userProfiles/avatar_user_${auth.currentUser?.uid}.jpg")
        avatarRef.delete().addOnSuccessListener {
            getProfileImage()
            Log.d(TAG, "ASuccessful delete user avatar image file")
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error delete user avatar image file: $exception")
        }

        // Step 4: Delete user Auth
        // TODO: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException:
        //      This operation is sensitive and requires recent authentication.
        //      Log in again before retrying this request.
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                // Step 5: Success
                Log.d(TAG, "Successful delete user Auth")
                showSnackbarMessage("User name updated.")
                logout()

            } else {
                Log.e(TAG, "Error delete user Auth: ${task.exception}")
            }
        }
    }
}
