package group.two.tripplanningapp.compose.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.utilities.SortOption

@Composable
fun SortingChips(
    onChipClick: (SortOption) -> Unit,
    isSortingOptionSelected: (SortOption) -> Boolean
) {
    Text(text = "Sort by:")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SortOption.entries.forEach { chip ->
            SortingChip(chip, onChipClick, isSortingOptionSelected(chip))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortingChip(
    sortOption: SortOption,
    onClick: (SortOption) -> Unit,
    selected: Boolean
) {
    FilterChip(
        onClick = { onClick(sortOption) },
        label = { Text(text = sortOption.name) },
        selected = selected
    )
}


@Preview
@Composable
fun SortingChipsPreview() {
    SortingChips(onChipClick = {}, isSortingOptionSelected = { false })
}