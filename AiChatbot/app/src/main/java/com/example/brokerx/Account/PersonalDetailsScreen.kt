package com.example.brokerx.Account

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brokerx.viewmodel.PersonalDetailsViewModel
import com.example.brokerx.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    personalDetailsViewModel: PersonalDetailsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = personalDetailsViewModel.isEditing
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by rememberSaveable { mutableStateOf("") }
    var reauthError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        personalDetailsViewModel.loadUserDetails()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal Details",
                        color = accentColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = accentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        personalDetailsViewModel.isEditing = !personalDetailsViewModel.isEditing
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        bottomBar = {
            if (isEditing) {
                Surface(color = surfaceColor) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = {
                                personalDetailsViewModel.saveDetails(
                                    personalDetailsViewModel.username,
                                    personalDetailsViewModel.contactNumber,
                                    personalDetailsViewModel.email,
                                    onReauthRequired = {
                                        showReauthDialog = true
                                    }
                                )
                                personalDetailsViewModel.isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(90.dp).padding(16.dp),
                tint = primaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            PersonalField(
                label = "User Name",
                value = personalDetailsViewModel.username,
                onValueChange = { personalDetailsViewModel.username = it },
                enabled = isEditing
            )

            Spacer(modifier = Modifier.height(24.dp))

            PersonalField(
                label = "Phone Number",
                value = personalDetailsViewModel.contactNumber,
                onValueChange = { personalDetailsViewModel.contactNumber = it },
                enabled = isEditing,
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(24.dp))

            PersonalField(
                label = "Email",
                value = personalDetailsViewModel.email,
                onValueChange = { personalDetailsViewModel.email = it },
                enabled = isEditing,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = { showReauthDialog = false },
            title = { Text("Re-authentication Required", color = accentColor) },
            text = {
                Column {
                    Text("Please enter your password to confirm this change.", color = primaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reauthPassword,
                        onValueChange = { reauthPassword = it },
                        label = { Text("Password", color = secondaryText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = secondaryText,
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = secondaryText,
                            cursorColor = primaryText,
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                            disabledTextColor = primaryText
                        )
                    )
                    if (reauthError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(reauthError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    personalDetailsViewModel.reauthenticate(
                        password = reauthPassword,
                        onSuccess = {
                            showReauthDialog = false
                            reauthPassword = ""
                            reauthError = null
                            personalDetailsViewModel.saveDetails(
                                personalDetailsViewModel.username,
                                personalDetailsViewModel.contactNumber,
                                personalDetailsViewModel.email,
                                onReauthRequired = { showReauthDialog = true }
                            )
                            personalDetailsViewModel.isEditing = false
                        },
                        onFailure = {
                            reauthError = "Authentication failed. Please try again."
                        }
                    )
                }) {
                    Text("Confirm", color = accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReauthDialog = false
                    reauthPassword = ""
                    reauthError = null
                }) {
                    Text("Cancel", color = secondaryText)
                }
            },
            containerColor = surfaceColor
        )
    }
}

@Composable
fun PersonalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = secondaryText) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = !enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = secondaryText,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = secondaryText,
            disabledTextColor = primaryText,
            cursorColor = primaryText,
            focusedTextColor = primaryText,
            unfocusedTextColor = primaryText
        )
    )
}