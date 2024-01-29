package group.two.tripplanningapp.data

import com.google.firebase.Timestamp

// Data classes representing user profile and reviews
data class Review(
    var reviewId: String,
    val creatorID: String,
    val content: String,
    val destination: String,
    val rating: Int,
    val timestamp: Timestamp
) {
    // no-argument constructor
    constructor() : this("", "", "","", 0, Timestamp.now())
}