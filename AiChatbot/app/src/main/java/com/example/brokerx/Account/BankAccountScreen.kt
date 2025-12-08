package com.example.brokerx.Account

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import com.example.brokerx.R
// Material 3
import androidx.compose.material3.*

// UI & Resources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

// Navigation
import androidx.navigation.NavController
import com.example.brokerx.data.model.BankAccount
import com.example.brokerx.viewmodels.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountScreen(
    navController: NavController,
    portfolioVM: PortfolioViewModel
) {
    val context = LocalContext.current
    val savedAccount = portfolioVM.bankAccount.observeAsState()
    val isEditing = rememberSaveable(savedAccount.value?.accountNumber) {
        mutableStateOf(savedAccount.value == null)
    }

    var bankName by rememberSaveable { mutableStateOf("") }
    var accountHolder by rememberSaveable { mutableStateOf("") }
    var accountNumber by rememberSaveable { mutableStateOf("") }
    val hasInitialized = rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(savedAccount.value) {
        if (!hasInitialized.value && savedAccount.value != null) {
            bankName = savedAccount.value!!.bankName
            accountHolder = savedAccount.value!!.accountHolder
            accountNumber = savedAccount.value!!.accountNumber
            hasInitialized.value = true
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Bank Account", color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = accentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isEditing.value = !isEditing.value }) {
                        Icon(
                            imageVector = if (isEditing.value) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing.value) "Save" else "Edit",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        bottomBar = {
            if (isEditing.value) {
                Surface(color = surfaceColor) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = {
                                val account = BankAccount(bankName, accountHolder, accountNumber)
                                portfolioVM.saveBankAccount(account)
                                isEditing.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Save Bank Account", color = Color.White)
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
                painter = painterResource(R.drawable.baseline_account_balance_wallet_24),
                contentDescription = "Bank Icon",
                modifier = Modifier.size(90.dp).padding(16.dp),
                tint = primaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF00D4FF), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Bank Name
                    Text(
                        text = bankName.ifBlank { "Bank Name" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Account Number (masked)
                    Text(
                        text = "•••• •••• •••• ${accountNumber.takeLast(4).padStart(4, 'X')}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Account Holder
                    Text(
                        text = accountHolder.ifBlank { "Account Holder" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PersonalField(
                label = "Bank Name",
                value = bankName,
                onValueChange = { bankName = it },
                enabled = isEditing.value
            )

            Spacer(modifier = Modifier.height(24.dp))

            PersonalField(
                label = "Account Holder",
                value = accountHolder,
                onValueChange = { accountHolder = it },
                enabled = isEditing.value
            )

            Spacer(modifier = Modifier.height(24.dp))

            PersonalField(
                label = "Account Number",
                value = accountNumber,
                onValueChange = { accountNumber = it },
                enabled = isEditing.value,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
