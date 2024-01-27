package group.two.tripplanningapp.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun FailureDialogHandler(openAlertDialog: Boolean, alertDialogMessage: String, setOpenAlertDialog: (Boolean) -> Unit) {
    when (openAlertDialog) {
        true -> {
            FailureDialog(
                onDismissRequest = { setOpenAlertDialog(false) },
                errorMessage = alertDialogMessage
            )
        }

        else -> {}
    }
}


@Composable
fun FailureDialog(
    onDismissRequest: () -> Unit,
    errorMessage: String
) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Oops!")
        },
        text = {
            Text(text = errorMessage)
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Ok, gotcha")
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Error, contentDescription = errorMessage)
        }
    )
}