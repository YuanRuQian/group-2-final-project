package group.two.tripplanningapp.utilities

import android.util.Log
import android.util.Patterns
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import group.two.tripplanningapp.data.Rating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun calculateAverageRating(rating: Rating): Double {
    val totalReviews =
        rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars

    return if (totalReviews > 0) {
        (rating.oneStar * 1 + rating.twoStars * 2 + rating.threeStars * 3 + rating.fourStars * 4 + rating.fiveStars * 5).toDouble() / totalReviews
    } else {
        0.0
    }
}


fun loadImageUrlFromFirebaseStorageUri(uri: String, setUrl: (String) -> Unit, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        // path example: gs://bucket/images/stars.jpg
        val imageRef = Firebase.storage.getReferenceFromUrl(uri)
        val url = imageRef.downloadUrl.await().toString()
        Log.d("DestinationsViewModel", "Image url: $url")
        setUrl(url)
    }
}