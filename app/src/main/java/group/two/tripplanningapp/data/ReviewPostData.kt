package group.two.tripplanningapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ReviewPostData(
    val content: String = "",
    val creatorID: String = "",
    val destination: String = "",
    val rating: Int = 0,
    @ServerTimestamp
    val timeCreated: Timestamp? = null,
    @ServerTimestamp
    val timeEdited: Timestamp? = null,
)
