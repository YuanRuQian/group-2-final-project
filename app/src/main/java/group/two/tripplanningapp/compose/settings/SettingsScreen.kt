package group.two.tripplanningapp.compose.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.data.LocaleConstant
import group.two.tripplanningapp.viewModels.SettingsViewModel
import group.two.tripplanningapp.compose.RatingBar

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    localeConstants: List<LocaleConstant>
) {
    val (selectedLocaleConstant, setSelectedLocaleConstant) = remember { mutableStateOf(if(localeConstants.isNotEmpty())localeConstants[0] else LocaleConstant()) }
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    val successMessage by settingsViewModel.successMessage.observeAsState()
    successMessage?.let {
        Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
        settingsViewModel.successMessage.value = null
    }

    val errorMessage by settingsViewModel.errorMessage.observeAsState()
    errorMessage?.let {
        Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
        settingsViewModel.errorMessage.value = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Choose Currency")
        CurrencyDropdown(
            localeConstants,
            expanded, setExpanded, selectedLocaleConstant, setSelectedLocaleConstant)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Feedback")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type your feedback here...") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        RatingBar(rating = rating, onRatingChange = { rating = it })

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isSubmitting = true
                settingsViewModel.submitFeedback(feedbackText, rating)
                feedbackText = ""
                rating = 0 // Reset rating
                isSubmitting = false
            },
            enabled = !isSubmitting
        ) {
            Text("Submit Feedback")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    localeConstants: List<LocaleConstant>,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    selectedLocaleConstant: LocaleConstant,
    setSelectedLocaleConstant: (LocaleConstant) -> Unit
) {
    val uniqueLocaleConstants = localeConstants.distinctBy { it.currencyCode }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { setExpanded(it) }) {
        CompositionLocalProvider(LocalTextInputService provides null) {
            TextField(
                readOnly = true,
                value = selectedLocaleConstant.currencyCode,
                onValueChange = {},
                label = { Text("Currency") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            uniqueLocaleConstants.forEach { localeConstant ->
                DropdownMenuItem(text = {
                    Text(text = localeConstant.currencyCode)
                }, onClick = {
                    setSelectedLocaleConstant(localeConstant)
                    setExpanded(false)
                })
            }
        }
    }
}

