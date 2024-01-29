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
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.data.DestinationTag

@Composable
fun TagsFilters(
    chips: List<DestinationTag>,
    onChipClick: (DestinationTag) -> Unit,
    isTagSelected: (DestinationTag) -> Boolean
) {
    Text(text = "Filter by Tags:")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        chips.forEach { chip ->
            TagChip(chip, onChipClick, isTagSelected(chip))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagChip(
    tag: DestinationTag,
    onClick: (DestinationTag) -> Unit,
    selected: Boolean
) {
    FilterChip(
        onClick = { onClick(tag) },
        label = { Text(text = tag.tagName) },
        selected = selected
    )
}
