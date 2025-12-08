package com.example.brokerx.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brokerx.R

// Shared color scheme
val backgroundColor = Color(0xFF0A0A0A)
val surfaceColor = Color(0xFF141414)
val primaryText = Color.White
val secondaryText = Color(0xFF888888)
val accentColor = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Effective Date: July 20, 2025",
                color = accentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = longPrivacyText,
                color = primaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private val longPrivacyText = """
At BrokerX, your privacy and trust are very important to us. This Privacy Policy outlines how we collect, use, store, and protect your personal information when you use our application and services. By accessing or using our platform, you acknowledge that you have read, understood, and agreed to the terms of this policy.

When you register an account with BrokerX, we collect certain personally identifiable information such as your name, email address, phone number, date of birth, and identification documents. This information is necessary for identity verification, account security, and compliance with legal and regulatory obligations such as Know Your Customer (KYC) and Anti-Money Laundering (AML) laws. Additionally, we collect financial and transactional information including your linked bank accounts, trading activity, and balances, all of which are essential for processing trades and maintaining your account.

We also collect technical information automatically when you use the app. This includes your device type, IP address, operating system, app usage data, and location (if enabled), which helps us improve user experience, enhance security, and ensure the functionality of our services. Cookies and similar technologies may be used to collect analytics data and personalize your interaction with the app.

The information we collect is used primarily to provide you with a secure and efficient trading experience. This includes verifying your identity, facilitating financial transactions, analyzing usage patterns to improve our services, and sending you important notifications and updates. We may also use your contact details to inform you of promotions or new features, but only with your prior consent.

We do not sell, rent, or trade your personal information to third parties. However, we may share your data with trusted service providers who perform functions on our behalf, such as cloud hosting, payment processing, customer verification, and regulatory compliance tools. These third parties are contractually obligated to protect your information and use it only for the purposes we specify.

To protect your data, we implement a variety of security measures, including data encryption, secure socket layer (SSL) technology, two-factor authentication (2FA), and periodic security audits. While we take reasonable steps to safeguard your personal information, no system can be guaranteed 100% secure. Therefore, we encourage you to protect your account credentials and report any suspicious activity immediately.

If you reside outside our primary operating country, your information may be transferred to and processed in countries where our servers or service providers are located. We take appropriate steps to ensure that your data is handled securely in accordance with this policy and applicable privacy laws.

You have rights regarding your personal information, including the right to access, correct, or delete your data, and to withdraw your consent for specific data uses. To exercise these rights or make a complaint, you may contact us using the details provided below.

This app and its services are not intended for individuals under the age of 18. We do not knowingly collect or store personal information from children.

We may update this Privacy Policy from time to time to reflect changes in our practices or legal requirements. If significant changes are made, we will notify you through the app or other appropriate means. Your continued use of the platform after such changes constitutes acceptance of the revised policy.

If you have any questions, concerns, or requests regarding this Privacy Policy or how your data is handled, please contact us at:
Email: privacy@brokerapp.com
Phone: +60 12-345 6789
Address: BrokerX Technologies Sdn. Bhd., Level 12, Menara BrokerX, Jalan Teknologi, Petaling Jaya, Selangor, Malaysia

By using our app, you agree to the collection and use of your information as outlined in this Privacy Policy.
""".trimIndent()
