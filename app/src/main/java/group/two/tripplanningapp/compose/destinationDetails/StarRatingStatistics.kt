package group.two.tripplanningapp.compose.destinationDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.data.Rating
import group.two.tripplanningapp.utilities.calculateAverageRating

@Composable
fun StarRatingStatistics(rating: Rating) {

    val (total, setTotal) = remember {
        mutableIntStateOf(rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars)
    }
    val (averageRating, setAverageRating) = remember {
        mutableStateOf(calculateAverageRating(rating))
    }
    val (averageRatingFormattedString, setAverageRatingFormattedString) = remember {
        mutableStateOf("%.2f".format(averageRating))
    }

    LaunchedEffect(key1 = rating.oneStar) {
        setTotal(rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars)
        setAverageRating(calculateAverageRating(rating))
        setAverageRatingFormattedString("%.2f".format(averageRating))
    }

    LaunchedEffect(key1 = rating.twoStars) {
        setTotal(rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars)
        setAverageRating(calculateAverageRating(rating))
        setAverageRatingFormattedString("%.2f".format(averageRating))
    }

    LaunchedEffect(key1 = rating.threeStars) {
        setTotal(rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars)
        setAverageRating(calculateAverageRating(rating))
        setAverageRatingFormattedString("%.2f".format(averageRating))
    }

    LaunchedEffect(key1 = rating.fourStars) {
        setTotal(rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars)
        setAverageRating(calculateAverageRating(rating))
        setAverageRatingFormattedString("%.2f".format(averageRating))
    }

    LaunchedEffect(key1 = rating.fiveStars) {
        setTotal(rating.oneStar + rating.twoStars + rating.threeStars + rating.fourStars + rating.fiveStars)
        setAverageRating(calculateAverageRating(rating))
        setAverageRatingFormattedString("%.2f".format(averageRating))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Rating Statistics",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Total: $averageRatingFormattedString out of 5 stars",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "$total rating${if (total > 1) "s" else ""}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        StarRow(
            starCount = 5,
            ratingsCount = rating.fiveStars,
            ratingsCountInTotal = total
        )

        StarRow(
            starCount = 4,
            ratingsCount = rating.fourStars,
            ratingsCountInTotal = total
        )

        StarRow(
            starCount = 3,
            ratingsCount = rating.threeStars,
            ratingsCountInTotal = total
        )

        StarRow(
            starCount = 2,
            ratingsCount = rating.twoStars,
            ratingsCountInTotal = total
        )

        StarRow(
            starCount = 1,
            ratingsCount = rating.oneStar,
            ratingsCountInTotal = total
        )
    }
}

@Composable
fun StarRow(starCount: Int, ratingsCount: Int, ratingsCountInTotal: Int) {
    val formattedPercentage = "%.2f".format(ratingsCount / ratingsCountInTotal.toFloat() * 100)

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$starCount star",
            modifier = Modifier.weight(2f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = ratingsCount / ratingsCountInTotal.toFloat(),
            modifier = Modifier
                .height(4.dp)
                .weight(6f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$formattedPercentage%",
            modifier = Modifier.weight(2f)
        )
    }
}


@Composable
@Preview
fun StarRatingStatisticsPreview() {
    val sampleRating = Rating(
        oneStar = 10,
        twoStars = 15,
        threeStars = 20,
        fourStars = 25,
        fiveStars = 30
    )
    StarRatingStatistics(rating = sampleRating)
}

