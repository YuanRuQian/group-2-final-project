package group.two.tripplanningapp.data

import group.two.tripplanningapp.compose.trips.Privacy

data class Trips(
    val trips: List<Trip> = emptyList()
)

data class Trip(
    var tripName: String = "",
    var numberOfPeople: Int = 0,
    var privacy: Privacy = Privacy.Private,
    var destinations: List<String> = emptyList(),
)

data class TripFields(
    var tripName: String = "",
    var numberOfPeople: Int = 0,
    var privacy: Boolean = false,
)
