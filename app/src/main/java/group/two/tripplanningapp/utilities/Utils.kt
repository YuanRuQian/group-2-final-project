package group.two.tripplanningapp.utilities

import android.graphics.BitmapFactory
import android.util.Patterns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import group.two.tripplanningapp.data.Rating

fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun calculateAverageRating(rating: Rating): Double {
    val totalReviews = rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars

    return if (totalReviews > 0) {
        (rating.oneStar * 1 + rating.twoStars * 2 + rating.threeStars * 3 + rating.fourStars * 4 + rating.fiveStars * 5).toDouble() / totalReviews
    } else {
        0.0
    }
}

fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap? {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
}