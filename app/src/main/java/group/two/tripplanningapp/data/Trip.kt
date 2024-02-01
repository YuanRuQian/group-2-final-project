package group.two.tripplanningapp.data

import group.two.tripplanningapp.compose.trips.Privacy

data class Trip(
    var tripName: String,
    var numberOfPeople: Int,
    var privacy: Privacy, // Assuming Privacy is an enum class with Private and Public options
    var destinations: List<String>
)

data class TripId(
    var id: String
)

data class TripFields(
    var tripName: String,
    var numberOfPeople: Int,
    var privacy: Boolean
)
