package group.two.tripplanningapp.compose.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import group.two.tripplanningapp.compose.RatingBar
import group.two.tripplanningapp.data.LocaleConstant
import group.two.tripplanningapp.viewModels.SettingsViewModel

// TODO: preload previous feedback and rating
// TODO: same currency code can be used for different countries, how to handle case like EUR for both France and Germany?
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    localeConstants: List<LocaleConstant>,
    showSnackbarMessage: (String) -> Unit,
    loadCurrentUserLocaleConstantCode: () -> Unit
) {
    val (selectedLocaleConstant, setSelectedLocaleConstant) = remember { mutableStateOf(if (localeConstants.isNotEmpty()) localeConstants[0] else LocaleConstant()) }
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (enabled, setEnabled) = remember { mutableStateOf(false) }

    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    val successMessage by settingsViewModel.successMessage.observeAsState()
    successMessage?.let {
        showSnackbarMessage(it)
        settingsViewModel.successMessage.value = null
    }

    val errorMessage by settingsViewModel.errorMessage.observeAsState()
    errorMessage?.let {
        Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
        settingsViewModel.errorMessage.value = null
    }

    fun checkIfFeedbackSubmitButtonShouldBeEnabled() {
        setEnabled(feedbackText.isNotBlank() && rating > 0)
    }

    val currentUserLocaleConstantCode =
        settingsViewModel.currentUserLocaleConstantCode.observeAsState()

    LaunchedEffect(key1 = currentUserLocaleConstantCode.value) {
        val localeConstantCode = currentUserLocaleConstantCode.value
        if (localeConstantCode != null) {
            val localeConstant = localeConstants.find { it.code == localeConstantCode }
            if (localeConstant != null) {
                setSelectedLocaleConstant(localeConstant)
            }
        }
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
            expanded,
            setExpanded,
            selectedLocaleConstant,
            setSelectedLocaleConstant,
            settingsViewModel::updateLocaleConstantCode,
            loadCurrentUserLocaleConstantCode
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Feedback")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = feedbackText,
            onValueChange = {
                feedbackText = it
                checkIfFeedbackSubmitButtonShouldBeEnabled()
            },
            placeholder = { Text("Enter your feedback here...") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        RatingBar(rating = rating, onRatingChange = {
            rating = it
            checkIfFeedbackSubmitButtonShouldBeEnabled()
        })

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = enabled,
            onClick = {
                setEnabled(false)
                settingsViewModel.submitFeedback(feedbackText, rating)
                feedbackText = ""
                rating = 0 // Reset rating
                checkIfFeedbackSubmitButtonShouldBeEnabled()
            },
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
    setSelectedLocaleConstant: (LocaleConstant) -> Unit,
    updateLocaleConstantCode: (String) -> Unit,
    loadCurrentUserLocaleConstantCode: () -> Unit
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
                    updateLocaleConstantCode(localeConstant.code)
                    loadCurrentUserLocaleConstantCode()
                    setExpanded(false)
                })
            }
        }
    }
}

