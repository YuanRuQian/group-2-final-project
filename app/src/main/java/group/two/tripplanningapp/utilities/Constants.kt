package group.two.tripplanningapp.utilities

enum class Region(val region: String, val currencyCode: String) {
    UNITED_STATES("United States", "USD"),
    EUROPEAN_UNION("European Union", "EUR"),
    JAPAN("Japan", "JPY"),
    AUSTRALIA("Australia", "AUD"),
    KOREA("Korea", "KRW"),
}


enum class SortOption {
    Name,
    Likes,
    Rating
}