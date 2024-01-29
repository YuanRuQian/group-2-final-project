package group.two.tripplanningapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

// Data classes representing user profile and reviews
data class Review(
    var reviewId: String = "",
    val creatorID: String = "",
    val content: String = "",
    val destination: String = "",
    val rating: Int = 0,
    @ServerTimestamp
    val timestamp: Timestamp? = null
)