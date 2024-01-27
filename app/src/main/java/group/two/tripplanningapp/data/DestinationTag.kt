package group.two.tripplanningapp.data

import com.google.firebase.firestore.DocumentSnapshot

data class DestinationTag(
    val tagName: String = "",
    val destinations: List<String> = emptyList()
)

