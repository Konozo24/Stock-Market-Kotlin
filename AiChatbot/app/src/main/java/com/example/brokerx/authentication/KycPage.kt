package com.example.brokerx.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.brokerx.R
import com.example.brokerx.data.local.UserPreferences
import com.example.brokerx.data.local.UserPrefsSingleton
import com.example.brokerx.viewmodel.KycViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycPage(modifier: Modifier = Modifier, navController: NavController) {
    val kycViewModel: KycViewModel = viewModel()

    // Collect single state object
    val formState by kycViewModel.formState.collectAsState()
    val isDobValid = kycViewModel.isValidDate(formState.dob)
    val isExpiryValid = kycViewModel.isValidDate(formState.idExpiry)
    val isFormValid by kycViewModel.isFormValid.collectAsState(initial = false)

    // Dropdown controls - using rememberSaveable for rotation
    var genderExpanded by rememberSaveable { mutableStateOf(false) }
    var idExpanded by rememberSaveable { mutableStateOf(false) }
    var incomeExpanded by rememberSaveable { mutableStateOf(false) }
    var expExpanded by rememberSaveable { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Other")
    val idOptions = listOf("Passport", "National ID", "Driver's License")
    val incomeOptions = listOf("<10k", "10k-50k", "50k-100k", ">100k")
    val expOptions = listOf("Beginner", "Intermediate", "Advanced")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1419),
                        Color(0xFF1A1F2E)
                    )
                )
            )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "Complete Your Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Please provide accurate information for account verification",
                fontSize = 14.sp,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Personal Information Section
            SectionCard(
                title = "Personal Information",
                icon = Icons.Default.Person
            ) {
                KycTextField(
                    value = formState.fullName,
                    onValueChange = { kycViewModel.updateForm { copy(fullName = it) } },
                    label = "Full Name (as per ID)",
                    painter = painterResource(id = R.drawable.outline_badge_24),
                    isError = formState.fullName.isNotEmpty().not()
                )

                KycTextField(
                    value = formState.dob,
                    onValueChange = { kycViewModel.updateForm { copy(dob = it) } },
                    label = "Date of Birth (DD-MM-YYYY)",
                    painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                    isError = !isDobValid
                )

                KycDropdown(
                    value = formState.gender,
                    onValueChange = { kycViewModel.updateForm { copy(gender = it) } },
                    label = "Gender",
                    options = genderOptions,
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it },
                    icon = Icons.Default.Person,
                )

                KycTextField(
                    value = formState.nationality,
                    onValueChange = { kycViewModel.updateForm { copy(nationality = it) } },
                    label = "Nationality",
                    painter = painterResource(id = R.drawable.baseline_flag_24),
                    isError = formState.nationality.isNotEmpty().not()

                )

                KycTextField(
                    value = formState.email,
                    onValueChange = {},
                    label = "Email",
                    icon = Icons.Default.Email,
                    enabled = false,
                )

                KycTextField(
                    value = formState.contactNumber,
                    onValueChange = { kycViewModel.updateForm { copy(contactNumber = it) } },
                    label = "Contact Number",
                    icon = Icons.Default.Phone,
                    isError = !formState.contactNumber.matches(Regex("^\\d{10,12}$"))
                )

                KycTextField(
                    value = formState.address,
                    onValueChange = { kycViewModel.updateForm { copy(address = it) } },
                    label = "Residential Address",
                    icon = Icons.Default.Home,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Identity Verification Section
            SectionCard(
                title = "Identity Verification",
                painter = painterResource(id = R.drawable.outline_verified_user_24)
            ) {
                KycDropdown(
                    value = formState.idType,
                    onValueChange = { kycViewModel.updateForm { copy(idType = it) } },
                    label = "ID Type",
                    options = idOptions,
                    expanded = idExpanded,
                    onExpandedChange = { idExpanded = it },
                    painter = painterResource(id = R.drawable.baseline_credit_card_24)
                )

                KycTextField(
                    value = formState.idNumber,
                    onValueChange = { kycViewModel.updateForm { copy(idNumber = it) } },
                    label = "ID Number",
                    painter = painterResource(id = R.drawable.outline_numbers_24)
                )

                KycTextField(
                    value = formState.idExpiry,
                    onValueChange = { kycViewModel.updateForm { copy(idExpiry = it) } },
                    label = "ID Expiry (DD-MM-YYYY)",
                    painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                    isError = !isExpiryValid
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Employment & Financial Section
            SectionCard(
                title = "Employment & Financial",
                icon = Icons.Default.Edit
            ) {
                KycTextField(
                    value = formState.occupation,
                    onValueChange = { kycViewModel.updateForm { copy(occupation = it) } },
                    label = "Occupation",
                    painter = painterResource(id = R.drawable.outline_sensor_occupied_24)
                )

                KycTextField(
                    value = formState.sourceOfFunds,
                    onValueChange = { kycViewModel.updateForm { copy(sourceOfFunds = it) } },
                    label = "Source of Funds",
                    painter = painterResource(id = R.drawable.baseline_account_balance_wallet_24)
                )

                KycDropdown(
                    value = formState.incomeBracket,
                    onValueChange = { kycViewModel.updateForm { copy(incomeBracket = it) } },
                    label = "Annual Income Bracket",
                    options = incomeOptions,
                    expanded = incomeExpanded,
                    onExpandedChange = { incomeExpanded = it },
                    painter = painterResource(id = R.drawable.outline_attach_money_24)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compliance & Risk Section
            SectionCard(
                title = "Compliance & Risk",
                icon = Icons.Default.Lock
            ) {
                KycTextField(
                    value = formState.tin,
                    onValueChange = { kycViewModel.updateForm { copy(tin = it) } },
                    label = "Tax Identification Number (TIN)",
                    painter = painterResource(id = R.drawable.baseline_receipt_long_24)
                )

                KycDropdown(
                    value = formState.investmentExperience,
                    onValueChange = { kycViewModel.updateForm { copy(investmentExperience = it) } },
                    label = "Investment Experience",
                    options = expOptions,
                    expanded = expExpanded,
                    onExpandedChange = { expExpanded = it },
                    painter = painterResource(id = R.drawable.baseline_bar_chart_24)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms and Conditions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E2328)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = formState.agreedToTerms,
                        onCheckedChange = { kycViewModel.updateForm { copy(agreedToTerms = it) } },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF00D4FF),
                            uncheckedColor = Color(0xFFB0BEC5),
                            checkmarkColor = Color(0xFF0F1419)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "I agree to the Terms & Conditions and Privacy Policy",
                        color = Color(0xFFE0E0E0),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val hasNavigated = rememberSaveable { mutableStateOf(false) }
            val userPreferences = UserPrefsSingleton.getInstance(LocalContext.current)
            val coroutineScope = rememberCoroutineScope() // Use this to launch coroutines

            // Submit Button
            Button(
                onClick = {
                    if (!hasNavigated.value) {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val userDoc = com.google.firebase.Firebase.firestore.collection("users").document(userId)
                            val kycData = mapOf(
                                "fullName" to formState.fullName,
                                "dob" to formState.dob,
                                "gender" to formState.gender,
                                "nationality" to formState.nationality,
                                "address" to formState.address,
                                "contactNumber" to formState.contactNumber,
                                "kycCompleted" to true
                            )
                            userDoc.update(kycData).addOnSuccessListener {
                                //  Save KYC completed in DataStore
                                coroutineScope.launch {
                                    userPreferences.saveKycCompleted(true)
                                }

                                navController.navigate("WatchList") {
                                    popUpTo("SignUp") { inclusive = true }
                                }
                                hasNavigated.value = true
                            }
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D4FF),
                    disabledContainerColor = Color(0xFF3A3F47),
                    contentColor = Color(0xFF0F1419),
                    disabledContentColor = Color(0xFF6B7280)
                )
            ) {
                Text(
                    text = "Complete Verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector? = null,
    painter: Painter? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2328)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                when {
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF00D4FF)
                    )
                    painter != null -> Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = Color(0xFF00D4FF)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KycTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    painter: Painter? = null,
    enabled: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFFB0BEC5)) },
        leadingIcon = {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF00D4FF)
                )
                painter != null -> Icon(
                    painter = painter,
                    contentDescription = label,
                    tint = Color(0xFF00D4FF)
                )
            }
        },
        enabled = enabled,
        maxLines = maxLines,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color.Red else Color(0xFF00D4FF),
            unfocusedBorderColor = if (isError) Color.Red else Color(0xFF3A3F47),
            errorBorderColor = Color.Red,
            errorLeadingIconColor = Color.Red,
            errorLabelColor = Color.Red,
            errorTrailingIconColor = Color.Red,
            errorSupportingTextColor = Color.Red,
            errorTextColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,
            cursorColor = Color(0xFF00D4FF)
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KycDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) },
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = Color(0xFFB0BEC5)) },
            leadingIcon = {
                when {
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF00D4FF)
                    )
                    painter != null -> Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = Color(0xFF00D4FF)
                    )
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D4FF),
                unfocusedBorderColor = Color(0xFF3A3F47),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFE0E0E0),
                cursorColor = Color(0xFF00D4FF)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color(0xFF1E2328))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Color.White
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        onExpandedChange(false)
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White,
                        leadingIconColor = Color(0xFF00D4FF)
                    )
                )
            }
        }
    }
}
