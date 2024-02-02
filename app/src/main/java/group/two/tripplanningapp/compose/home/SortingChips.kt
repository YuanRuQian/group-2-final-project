package group.two.tripplanningapp.compose.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.utilities.DestinationSortOption

@Composable
fun SortingChips(
    onChipClick: (DestinationSortOption) -> Unit,
    isSortingOptionSelected: (DestinationSortOption) -> Boolean
) {
    Text(text = "Sort by:")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DestinationSortOption.entries.forEach { chip ->
            SortingChip(chip, onChipClick, isSortingOptionSelected(chip))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortingChip(
    destinationSortOption: DestinationSortOption,
    onClick: (DestinationSortOption) -> Unit,
    selected: Boolean
) {
    FilterChip(
        onClick = { onClick(destinationSortOption) },
        label = { Text(text = destinationSortOption.name) },
        selected = selected
    )
}


@Preview
@Composable
fun SortingChipsPreview() {
    SortingChips(onChipClick = {}, isSortingOptionSelected = { false })
}