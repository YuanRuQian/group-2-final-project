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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import group.two.tripplanningapp.utilities.isEmailValid

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun LoginScreen(
    login: (String, String, () -> Unit, (String) -> Unit, (String) -> Unit) -> Unit,
    navigateToRegisterScreen: () -> Unit,
    navigateToHomeScreen: () -> Unit,
    showSnackbarMessage: (String) -> Unit,
    showDialog: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val (isEmailValid, setIsEmailValid) = remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val (isLoginButtonEnabled, setIsLoginButtonEnabled) = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun updateLoginButtonState() {
        setIsLoginButtonEnabled(
            email.isNotBlank() && password.isNotBlank() && isEmailValid
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Username TextField
        TextField(
            value = email,
            onValueChange = {
                email = it
                setIsEmailValid(isEmailValid(email))
                updateLoginButtonState()
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
            modifier = Modifier.testTag("password"),
            value = password,
            onValueChange = {
                password = it
                updateLoginButtonState()
            },
            singleLine = true,
            label = { Text("Password") },
            visualTransformation =
            if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    val description = if (isPasswordVisible) "Hide password" else "Show password"
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.padding(32.dp))

        Button(
            onClick = {
                login(email, password, navigateToHomeScreen, showSnackbarMessage, showDialog)
                keyboardController?.hide()
            },
            enabled = isLoginButtonEnabled,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .testTag("loginButton"),
        ) {
            Text(text = "Login")
        }

        TextButton(
            modifier = Modifier.testTag("registerText"),
            onClick = {
                navigateToRegisterScreen()
            }) {
            Text(text = "Don't have an account? Click here to register")
        }

    }
}
