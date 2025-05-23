package com.vlog.my.screens.videos.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

enum class PasswordDialogMode {
    SET_PASSWORD,
    ENTER_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordPromptDialog(
    mode: PasswordDialogMode,
    onDismiss: () -> Unit,
    onPasswordSet: (password: String) -> Unit, // For SET_PASSWORD mode
    onPasswordEntered: (password: String) -> Unit, // For ENTER_PASSWORD mode
    errorMessage: String? = null // To display errors like "Passwords don't match" or "Incorrect password"
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // Only used in SET_PASSWORD mode
    var internalErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        internalErrorMessage = errorMessage
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (mode == PasswordDialogMode.SET_PASSWORD) "Set Script Password" else "Enter Script Password",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; internalErrorMessage = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = internalErrorMessage != null
                )

                if (mode == PasswordDialogMode.SET_PASSWORD) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; internalErrorMessage = null },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = internalErrorMessage != null && internalErrorMessage?.contains("match") == true
                    )
                }

                internalErrorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (mode == PasswordDialogMode.SET_PASSWORD) {
                                if (password.isEmpty() || confirmPassword.isEmpty()) {
                                    internalErrorMessage = "Password fields cannot be empty."
                                } else if (password != confirmPassword) {
                                    internalErrorMessage = "Passwords do not match."
                                } else {
                                    onPasswordSet(password)
                                    // onDismiss() // Dialog should be dismissed by calling composable based on success
                                }
                            } else { // ENTER_PASSWORD mode
                                if (password.isEmpty()) {
                                    internalErrorMessage = "Password cannot be empty."
                                } else {
                                    onPasswordEntered(password)
                                    // onDismiss() // Dialog should be dismissed by calling composable based on success
                                }
                            }
                        }
                    ) {
                        Text(if (mode == PasswordDialogMode.SET_PASSWORD) "Set Password" else "Unlock")
                    }
                }
            }
        }
    }
}
