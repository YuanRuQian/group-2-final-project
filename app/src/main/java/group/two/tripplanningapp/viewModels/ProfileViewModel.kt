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


    // User Display Name
    fun getDisplayName():String{
        _displayName.value = user?.displayName ?: "Unknown User Name"
        Log.d(TAG, "getDisplayName: ${_profileImageUrl.value}")
        return displayName.value
    }
    fun updateDisplayName(newDisplayName: String, showSnackbarMessage: (String) -> Unit) {
        user!!.updateProfile(
            userProfileChangeRequest {
                displayName = newDisplayName
            }
        ).addOnCompleteListener{
            if (it.isSuccessful){
                showSnackbarMessage("User display name updated.")
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
    fun updateProfileImage(imageUri: Uri, showSnackbarMessage: (String) -> Unit) {
        // Step 1: Upload Image to Firebase Storage
        val storageRef: StorageReference = storage.reference
        val imageFileName = "avatar_user_${auth.currentUser?.uid}.jpg"
        val imageRef: StorageReference = storageRef.child("/images/userProfiles/$imageFileName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Step 2: Get Download URL and Update User Profile
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    user!!.updateProfile(
                        userProfileChangeRequest {
                            photoUri = uri
                        }
                    ).addOnCompleteListener{
                        if (it.isSuccessful){
                            Log.d(TAG, "User profile pic updated.")
                            showSnackbarMessage("User profile picture updated.")
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

    // User Reviews
    fun getUserReviews() {
        // Get Review IDs
        val reviewsCollection = firestore.collection("userProfiles").document(user?.uid?:"")
            .collection("reviews")

        reviewsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting user review IDs: $error")
                return@addSnapshotListener
            }
            // Step 1: Get User Reviews' IDs
            val userReviewIds = snapshot?.documents?.mapNotNull { document ->
                document.id
            } ?: emptyList()

            // Step 2: fetch reviews from the IDs
            fetchReviews(userReviewIds)
        }
    }
    private fun fetchReviews(reviewIds: List<String>) {
        // Initialize an empty list to store fetched reviews
        val fetchedReviews = mutableListOf<Review>()

        // Loop through each review ID and fetch the corresponding review
        reviewIds.forEach { reviewId ->
            val reviewDocumentRef =
                firestore.collection("reviews").document(reviewId)

            reviewDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                val review = documentSnapshot.toObject(Review::class.java)
                review?.let {
                    it.reviewId =reviewId
                    // Add the fetched review to the list
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
        var deleteInReview = false
        var deleteInUser = false

        // Step 1: Delete in reviews collection: reviews\{reviewID}
        val reviewDocumentRef =
            firestore.collection("reviews").document(reviewId)
        reviewDocumentRef.delete()
            .addOnSuccessListener {
                deleteInReview =true
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting review: $exception")
                showSnackbarMessage("Error deleting review.")
            }

        // Step 2: Delete in userProfile: userProfile\{userID}\reviews\{reviewID}
        val userReviewsCollectionRef = firestore.collection("userProfiles").document(user?.uid?:"")
            .collection("reviews").document(reviewId)
        userReviewsCollectionRef.delete()
            .addOnSuccessListener {
                deleteInUser = true
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting review: $exception")
                showSnackbarMessage("Error deleting review.")
            }

        // Post Deletion
        if (deleteInReview&&deleteInUser){
            showSnackbarMessage("Review deleted successfully.")
            // refresh the reviews after a deletion
            getUserReviews()
        }
    }


    // TO-DO: Implement delete Account Function
    fun deleteAccount(){

    }
}
