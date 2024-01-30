package group.two.tripplanningapp.viewModels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import androidx.compose.runtime.State
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.utilities.ProfileReviewSortOptions


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

    private val _reviews: MutableState<List<Review>> = mutableStateOf(emptyList())
    val reviews: MutableState<List<Review>> get() = _reviews

    init {
        getProfileImage()
        getDisplayName()
        getUserReviews()
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

    // User Reviews
    private fun getUserReviews() {
        // Get Review IDs
        val reviewsCollection = firestore.collection("userProfiles").document(user?.uid?:"")
            .collection("reviews")

        reviewsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting user review IDs: $error")
            }

            // Step 1: Get User Reviews' IDs as List<String>
            val userReviewIds = snapshot?.documents?.mapNotNull { document ->
                document.id
            } ?: emptyList()

            // Step 2: fetch reviews from the List of IDs
            fetchReviews(userReviewIds)
        }
    }
    private fun fetchReviews(reviewIds: List<String>) {
        // Initialize an empty list to store fetched reviews
        val fetchedReviews = mutableListOf<Review>()

        // Loop through each review ID and fetch the corresponding review
        reviewIds.forEach { reviewId ->
            val reviewDocumentRef = firestore.collection("reviews").document(reviewId)

            reviewDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                val review = documentSnapshot.toObject(Review::class.java)
                review?.let {
                    // Add the fetched review to the list
                    it.reviewId =reviewId
                    fetchedReviews.add(it)
                }

                // Check if all reviews have been fetched
                if (fetchedReviews.size == reviewIds.size) {
                    // Update with the fetched reviews
                    _reviews.value = fetchedReviews
                }

            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting review with ID $reviewId: $exception")
            }
        }
    }

    // Update Reviews
    fun updateReview(reviewId: String, newContent: String, showSnackbarMessage: (String) -> Unit) {
        val reviewDocumentRef =
            firestore.collection("reviews").document(reviewId)

        val newTimestamp = Timestamp.now()
        val updatedReview = hashMapOf(
            "content" to newContent,
            "timestamp" to newTimestamp
        )

        reviewDocumentRef.update(updatedReview.toMap())
            .addOnSuccessListener {
                showSnackbarMessage("Review updated successfully.")
                // Assuming you want to refresh the reviews after an update
                getUserReviews()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating review: $exception")
                showSnackbarMessage("Error updating review.")
            }
    }

    // Delete Reviews
    fun deleteReview(reviewId: String, showSnackbarMessage: (String) -> Unit) {

        // Step 1: Delete in reviews collection: reviews\{reviewID}
        val reviewDocumentRef =
            firestore.collection("reviews").document(reviewId)
        reviewDocumentRef.delete()
            .addOnSuccessListener {

                // Step 2: Delete in userProfile: userProfile\{userID}\reviews\{reviewID}
                val userReviewsCollectionRef =
                    firestore.collection("userProfiles").document(user?.uid ?: "")
                        .collection("reviews").document(reviewId)
                userReviewsCollectionRef.delete()
                    .addOnSuccessListener {

                        // Post Deletions
                        showSnackbarMessage("Review deleted successfully.")
                        getUserReviews() // refresh the reviews
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error deleting review: $exception")
                        showSnackbarMessage("Error deleting review.")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting review: $exception")
                showSnackbarMessage("Error deleting review.")
            }
    }

    // Function to get sorted reviews based on the selected sorting option
    fun profileSortReviews(sortOption: ProfileReviewSortOptions) {
        when (sortOption) {
            ProfileReviewSortOptions.Date -> _reviews.value = reviews.value.sortedByDescending { it.timestamp }
            ProfileReviewSortOptions.Location -> _reviews.value =reviews.value.sortedBy { it.destination }
            ProfileReviewSortOptions.Rating -> _reviews.value =reviews.value.sortedByDescending { it.rating }
        }
    }



    // Delete Account
    fun deleteAccount(showSnackbarMessage: (String) -> Unit, logout:()->Unit){

        // Step 1: Delete all reviews by the user in database
        _reviews.value.forEach{ eachReview ->
            deleteReview(eachReview.reviewId, showSnackbarMessage = {Log.e(TAG, "Delete Review: $eachReview")})
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
