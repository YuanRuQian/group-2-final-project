package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import group.two.tripplanningapp.data.Review
import group.two.tripplanningapp.utilities.ProfileReviewSortOptions

class ReviewViewModel : ViewModel()  {
    private val TAG = "TripApppDebug_ReviewViewModel"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val yourReviewIds : MutableState<List<String>> = mutableStateOf(emptyList())

    private val _yourReviews: MutableState<List<Review>> = mutableStateOf(emptyList())
    val yourReviews: MutableState<List<Review>> get() = _yourReviews

    private val _curDesReviews: MutableState<List<Review>> = mutableStateOf(emptyList())
    val curDesReviews: MutableState<List<Review>> get() = _curDesReviews

    init {
        getUserReviews()
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
            yourReviewIds.value = snapshot?.documents?.mapNotNull { document ->
                document.id
            } ?: emptyList()

            // Step 2: fetch reviews from the List of IDs
            fetchReviews(yourReviewIds.value, _yourReviews)
        }
    }
    fun getDestinationReviews(destinationID:String){
        // Get Review IDs
        val reviewsCollection = firestore.collection("destination").document(destinationID)
            .collection("reviews")

        reviewsCollection.addSnapshotListener { snapshot, error ->
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
        val fetchedReviews = mutableListOf<Review>()

        // Loop through each review ID and fetch the corresponding review
        reviewIds.forEach { reviewId ->
            val reviewDocumentRef = firestore.collection("reviews").document(reviewId)

            reviewDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                val review = documentSnapshot.toObject(Review::class.java)
                review?.let {
                    // Add the fetched review to the list
                    it.reviewId =reviewId
                    // Allow edit when comment it is yours
                    if (it.creatorID==user?.uid.toString()) it.editable = true
                    fetchedReviews.add(it)
                }

                // Check if all reviews have been fetched
                if (fetchedReviews.size == reviewIds.size) {
                    // Update with the fetched reviews
                    toBeFetched.value = fetchedReviews
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
            ProfileReviewSortOptions.Date -> _yourReviews.value = yourReviews.value.sortedByDescending { it.timeEdited}
            ProfileReviewSortOptions.Location -> _yourReviews.value =yourReviews.value.sortedBy { it.destination }
            ProfileReviewSortOptions.Rating -> _yourReviews.value =yourReviews.value.sortedByDescending { it.rating }
        }
    }
}