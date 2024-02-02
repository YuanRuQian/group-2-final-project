package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import group.two.tripplanningapp.data.Destination
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.data.ReviewPostData
import group.two.tripplanningapp.data.UserProfile
import group.two.tripplanningapp.utilities.ProfileReviewSortOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: fix all cards display the same avatar
class ReviewViewModel : ViewModel() {
    private val TAG = "TripApppDebug_ReviewViewModel"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val defaultAvatarURL: MutableState<String> = mutableStateOf("")
    private val yourReviewIds: MutableState<List<String>> = mutableStateOf(emptyList())

    private val _yourReviews: MutableState<List<Review>> = mutableStateOf(emptyList())
    val yourReviews: MutableState<List<Review>> get() = _yourReviews

    private val _curDesReviews: MutableState<List<Review>> = mutableStateOf(emptyList())
    val curDesReviews: MutableState<List<Review>> get() = _curDesReviews

    init {
        getUserReviews()
        getDefaultAvatar()
    }

    private fun getDefaultAvatar() {
        val defaultAvatarUri = "/images/userProfiles/avatar_default.webp"
        val imageRef: StorageReference = storage.reference.child(defaultAvatarUri)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            defaultAvatarURL.value = uri.toString()
            Log.d(TAG, "getProfileImage: ${defaultAvatarURL.value}")
        }
    }

    fun getReviewerAvatarAndName(
        userID: String,
        setAvatar: (String) -> Unit,
        setUsername: (String) -> Unit
    ) {
        Log.d("TripApppDebug", "getReviewerAvatarAndName: $userID, default url: ${defaultAvatarURL.value}")
        setAvatar(defaultAvatarURL.value)
        firestore.collection("userProfiles").document(userID).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UserProfile::class.java)
                if (user != null) {
                    Log.d("TripApppDebug", "it.avatar: ${user.avatar}")
                    if (!(user.avatar == "null" || user.avatar.isEmpty())) {
                        setAvatar(user.avatar)
                    }
                    setUsername(if (!(user.userName == "null" || user.userName.isEmpty())) user.userName else "Unknown User")
                } else {
                    setUsername("Unknown User")
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error Avatar And Name for user $userID: $exception")
            }

    }

    // User Reviews
    private fun getUserReviews() {
        Log.d(TAG, "getUserReviews: ")
        // Get Review IDs
        if (auth.currentUser == null) {
            return
        }

        val reviewsCollection =
            firestore.collection("userProfiles").document(auth.currentUser!!.uid)
                .collection("reviews")

        reviewsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting user review IDs: $error")
            }

            // Step 1: Get User Reviews' IDs as List<String>
            yourReviewIds.value = snapshot?.documents?.mapNotNull { document ->
                document.id
            } ?: emptyList()

            // Step 2: fetch reviews from the List of IDs
            fetchReviews(yourReviewIds.value, _yourReviews)
        }
    }

    fun getDestinationReviews(destinationID: String) {
        Log.d(TAG, "getDestinationReviews: $destinationID")
        _curDesReviews.value = emptyList()
        // Get Review IDs
        firestore.collection("destinations").document(destinationID)
            .collection("reviews").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting destination($destinationID) review IDs: $error")
            }

            // Step 1: Get User Reviews' IDs as List<String>
            val destinationReviewIds = snapshot?.documents?.mapNotNull { document ->
                document.id
            } ?: emptyList()

            // Step 2: fetch reviews from the List of IDs
            fetchReviews(destinationReviewIds, _curDesReviews)
        }
    }

    private fun fetchReviews(reviewIds: List<String>, toBeFetched: MutableState<List<Review>>) {
        // Initialize an empty list to store fetched reviews
        Log.d(TAG, "fetchReviews: reviewIds: $reviewIds")
        val fetchedReviews = mutableListOf<Review>()

        // Loop through each review ID and fetch the corresponding review
        reviewIds.forEach { reviewId ->
            val reviewDocumentRef = firestore.collection("reviews").document(reviewId)

            reviewDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                val review = documentSnapshot.toObject(Review::class.java)
                review?.let {
                    // Add the fetched review to the list
                    it.reviewId = reviewId
                    // Allow edit when comment it is yours
                    if (it.creatorID == auth.currentUser?.uid.toString()) it.editable = true
                    fetchedReviews.add(it)
                }

                // Check if all reviews have been fetched
                if (fetchedReviews.size == reviewIds.size) {
                    // Update with the fetched reviews
                    toBeFetched.value = fetchedReviews.sortedByDescending { it.timeCreated }
                    Log.d(TAG, "fetchReviews: success fetchedReviews: ${fetchedReviews.map { it.content }}")
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
            "timeEdited" to newTimestamp
        )

        reviewDocumentRef.update(updatedReview.toMap())
            .addOnSuccessListener {
                showSnackbarMessage("Review updated successfully.")
                // refresh the reviews after an update
                getUserReviews()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating review: $exception")
                showSnackbarMessage("Error updating review.")
            }
    }

    fun addReview(
        destinationID: String,
        content: String,
        rating: Int,
        showSnackbarMessage: (String) -> Unit
    ) {
        viewModelScope.launch {

            val reviewDocumentRef = firestore.collection("reviews").document()

            val destination = firestore.collection("destinations").document(destinationID)
                .get().await()

            val destinationName = destination.getString("name") ?: "Unknown Destination"

            val newReview = ReviewPostData(
                content = content,
                creatorID = auth.currentUser?.uid ?: "",
                destination = destinationName,
                rating = rating
            )

            reviewDocumentRef.set(newReview)
                .addOnSuccessListener {
                    // Step 1: Add in reviews collection: reviews\{reviewID}
                    val reviewID = reviewDocumentRef.id

                    // Step 2: Add in userProfile: userProfile\{userID}\reviews\{reviewID}
                    val userReviewsCollectionRef =
                        firestore.collection("userProfiles").document(auth.currentUser?.uid ?: "")
                            .collection("reviews").document(reviewID)
                    userReviewsCollectionRef.set(newReview)
                        .addOnSuccessListener {
                            showSnackbarMessage("Review added successfully.")
                            getUserReviews() // refresh the reviews
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error adding review: $exception")
                            showSnackbarMessage("Error adding review.")
                        }

                    // Step 3: update destination rating in destinations collection
                    val destinationDocumentRef =
                        firestore.collection("destinations").document(destinationID)
                    destinationDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                        documentSnapshot.toObject(Destination::class.java)?.let {
                            val newRating = it.copy(
                                rating =
                                when (rating) {
                                    1 -> it.rating.copy(oneStar = it.rating.oneStar + 1)
                                    2 -> it.rating.copy(twoStars = it.rating.twoStars + 1)
                                    3 -> it.rating.copy(threeStars = it.rating.threeStars + 1)
                                    4 -> it.rating.copy(fourStars = it.rating.fourStars + 1)
                                    5 -> it.rating.copy(fiveStars = it.rating.fiveStars + 1)
                                    else -> it.rating
                                }
                            )
                            destinationDocumentRef.set(newRating)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting destination: $exception")
                    }

                    // Step 4: Add review to destination's reviews sub-collection, with a custom document ID
                    val destinationRef =
                        firestore.collection("destinations").document(destinationID)

                    // Use a fixed document ID for the review within the "reviews" sub-collection

                    val newReviewDocument = hashMapOf(
                        "documentId" to reviewID
                    )

// Add the review document to the "reviews" sub-collection
                    destinationRef.collection("reviews").document(reviewID)
                        .set(newReviewDocument)
                        .addOnSuccessListener {
                            showSnackbarMessage("Review added successfully.")
                        }
                        .addOnFailureListener { e ->
                            // Handle failure, if needed
                            Log.e(
                                "Firestore",
                                "failed to add review to destination's reviews sub-collection",
                                e
                            )
                        }

                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error adding review: $exception")
                    showSnackbarMessage("Error adding review.")
                }
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
                    firestore.collection("userProfiles").document(auth.currentUser?.uid ?: "")
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
            ProfileReviewSortOptions.Date -> _yourReviews.value =
                yourReviews.value.sortedByDescending { it.timeEdited }

            ProfileReviewSortOptions.Location -> _yourReviews.value =
                yourReviews.value.sortedBy { it.destination }

            ProfileReviewSortOptions.Rating -> _yourReviews.value =
                yourReviews.value.sortedByDescending { it.rating }
        }
    }

    fun clearData() {
        _yourReviews.value = emptyList()
        _curDesReviews.value = emptyList()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReviewViewModel()
            }
        }
    }
}