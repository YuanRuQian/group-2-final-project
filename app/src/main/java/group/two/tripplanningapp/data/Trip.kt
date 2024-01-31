package group.two.tripplanningapp.data

import group.two.tripplanningapp.compose.trips.Privacy

data class Trip(
    val tripName: String,
    val numberOfPeople: Int,
    var privacy: Privacy, // Assuming Privacy is an enum class with Private and Public options
    val destinations: List<String>
)
