package com.example.brokerx.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.R
import com.example.brokerx.viewmodels.PortfolioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    portfolioViewModel: PortfolioViewModel,
    navController: NavController
) {
    val wallet by portfolioViewModel.wallet.observeAsState()
    var amount by rememberSaveable { mutableStateOf("") }
    var isProcessing by rememberSaveable { mutableStateOf(false) }
    var showSuccess by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val backgroundColor = Color(0xFF0A0A0A)
    val surfaceColor = Color(0xFF141414)
    val primaryText = Color.White
    val secondaryText = Color(0xFF888888)
    val accentColor = Color(0xFF1976D2)
    val successColor = Color(0xFF00D4AA)

    // Quick amount suggestions
    val quickAmounts = listOf(100.0, 500.0, 1000.0, 2500.0)

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Funds",
                        color = primaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = primaryText
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Current Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Available Balance",
                        fontSize = 14.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$${String.format("%,.2f", wallet?.cash ?: 0.0)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.W600,
                        color = primaryText,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Amount Input Section
            Text(
                text = "Deposit Amount",
                fontSize = 16.sp,
                fontWeight = FontWeight.W500,
                color = primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            // Custom Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    // Only allow numbers and decimal point
                    if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = it
                    }
                },
                placeholder = {
                    Text(
                        "0.00",
                        color = secondaryText.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Text(
                        text = "$",
                        color = primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W500
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    cursorColor = accentColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Amount Buttons
            Text(
                text = "Quick Add",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                quickAmounts.forEach { quickAmount ->
                    QuickAmountButton(
                        amount = quickAmount,
                        modifier = Modifier.weight(1f),
                        surfaceColor = surfaceColor,
                        primaryText = primaryText,
                        accentColor = accentColor,
                        onClick = {
                            amount = quickAmount.toInt().toString()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Deposit Button
            val isValidAmount = amount.toDoubleOrNull()?.let { it > 0 } == true

            Button(
                onClick = {
                    val depositAmount = amount.toDoubleOrNull()
                    if (depositAmount != null && depositAmount > 0) {
                        scope.launch {
                            // Start processing
                            isProcessing = true
                            showSuccess = false

                            // Simulate a delay for "depositing"
                            delay(1500)

                            // Update wallet / portfolio
                            portfolioViewModel.deposit(depositAmount)

                            // Stop processing, show success
                            isProcessing = false
                            showSuccess = true

                            // Keep success message on screen briefly
                            delay(1000)

                            // Navigate back safely on main thread
                            withContext(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                enabled = isValidAmount && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showSuccess) successColor else accentColor,
                    disabledContainerColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                when {
                    showSuccess -> {
                        Text(
                            text = "✓ Deposit Successful",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600,
                            color = Color.White
                        )
                    }
                    isProcessing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Processing...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W500,
                                color = Color.White
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Add $${if (amount.isNotEmpty()) amount else "0"} to Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Notice
            Text(
                text = "🔒 Your funds are protected with bank-level security",
                fontSize = 12.sp,
                color = secondaryText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickAmountButton(
    amount: Double,
    modifier: Modifier = Modifier,
    surfaceColor: Color,
    primaryText: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$${amount.toInt()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.W500,
            color = primaryText
        )
    }
}