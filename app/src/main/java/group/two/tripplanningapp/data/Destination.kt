package group.two.tripplanningapp.data

data class Destination(
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val averageCostPerPersonInCents: Int = 0,
    val likes: Int = 0,
    val activities: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val rating: Rating = Rating()
)

data class Rating(
    val oneStar: Int = 0,
    val twoStars: Int = 0,
    val threeStars: Int = 0,
    val fourStars: Int = 0,
    val fiveStars: Int = 0
)
