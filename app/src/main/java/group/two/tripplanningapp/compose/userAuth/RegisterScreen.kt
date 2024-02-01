package group.two.tripplanningapp.compose.userAuth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardType.Companion.Password
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.data.LocaleConstant
import group.two.tripplanningapp.utilities.isEmailValid

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen(
    localeConstants: List<LocaleConstant>,
    signup: (String, String, String, LocaleConstant, () -> Unit, (String) -> Unit, (String) -> Unit) -> Unit,
    navigateToLoginScreen: () -> Unit,
    navigateToHomeScreen: () -> Unit,
    showSnackbarMessage: (String) -> Unit,
    showDialog: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val (isEmailValid, setIsEmailValid) = remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val (isRegisterButtonEnabled, setIsRegisterButtonEnabled) = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    // TODO: use GPS to get the user's region as default
    val (selectedLocaleConstant, setSelectedLocaleConstant) = remember { mutableStateOf(if(localeConstants.isNotEmpty())localeConstants[0] else LocaleConstant()) }
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    fun updateRegisterButtonState() {
        setIsRegisterButtonEnabled(
            username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && isEmailValid
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = username,
            onValueChange = {
                username = it
                updateRegisterButtonState()
            },
            label = { Text(text = "Username") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.padding(16.dp))

        TextField(
            value = email,
            onValueChange = {
                email = it
                setIsEmailValid(isEmailValid(email))
                updateRegisterButtonState()
            },
            label = { Text(if (!isEmailValid) "Email*" else "Email") },
            singleLine = true,
            supportingText = {
                if (!isEmailValid) {
                    Text(
                        text = "Please enter a valid email address",
                    )
                }
            },
            isError = !isEmailValid,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.padding(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                updateRegisterButtonState()
            },
            singleLine = true,
            label = { Text("Password") },
            visualTransformation =
            if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = Password),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    val visibilityIcon =
                        if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    val description =
                        if (isPasswordVisible) "Hide password" else "Show password"
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.padding(16.dp))

        LocaleDropdown(
            localeConstants,
            expanded, setExpanded, selectedLocaleConstant, setSelectedLocaleConstant)

        Spacer(modifier = Modifier.padding(32.dp))

        Button(
            onClick = {
                signup(email, password, username, selectedLocaleConstant, navigateToHomeScreen, showSnackbarMessage, showDialog)
                keyboardController?.hide()
            },
            enabled = isRegisterButtonEnabled,
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Register")
        }

        TextButton(
            modifier = Modifier.testTag("loginText"),
            onClick = {
                navigateToLoginScreen()
            }
        ) {
            Text(text = "Already have an account? Click here to login")
        }
    }
}

// TODO: need future improvement for locale area code data and other stuff...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocaleDropdown(
    localeConstants: List<LocaleConstant>,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    selectedLocaleConstant: LocaleConstant,
    setSelectedLocaleConstant: (LocaleConstant) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { setExpanded(it) }) {
        CompositionLocalProvider(LocalTextInputService provides null) {
            TextField(
                readOnly = true,
                value = selectedLocaleConstant.displayName,
                onValueChange = {},
                label = { Text("Region") },
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
            localeConstants.forEach { localeConstant ->
                DropdownMenuItem(text = {
                    Text(text = localeConstant.displayName)
                }, onClick = {
                    setSelectedLocaleConstant(localeConstant)
                    setExpanded(false)
                })
            }
        }
    }
}