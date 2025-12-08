
package com.example.brokerx.Account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.brokerx.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Your color scheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms & Conditions",
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
                text = longTermsText,
                color = primaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private val longTermsText = """
Welcome to BrokerX, a platform operated by BrokerX Technologies Sdn. Bhd. (“we,” “us,” or “our”). These Terms and Conditions (“Terms”) govern your access to and use of our mobile application, website, and related services (collectively, the “Platform”). By accessing or using the Platform, you agree to comply with and be bound by these Terms.

To use our services, you must be at least 18 years of age or the legal age of majority in your jurisdiction. By creating an account, you represent and warrant that all information you provide is accurate, current, and complete, and that you will maintain the accuracy of such information at all times. You are solely responsible for safeguarding your login credentials and for all activities that occur under your account.

BrokerX offers users the ability to access financial markets, execute trades, monitor portfolios, and utilize various analytics tools. These services are provided on an “as is” and “as available” basis. We reserve the right to modify, suspend, or discontinue any part of the Platform at our sole discretion, without prior notice. While we aim to provide accurate data and uninterrupted access, we do not guarantee the reliability, availability, or accuracy of any content or data provided through the Platform.

It is important to understand that trading in financial markets involves significant risk, including the loss of your invested capital. Past performance is not indicative of future results. By using our services, you acknowledge that you are fully aware of these risks and that you are solely responsible for any losses or gains resulting from your trading decisions.

Our services may be subject to transaction fees, commissions, spreads, or other charges, as outlined within the Platform or in specific product disclosures. You agree to pay all applicable fees and charges incurred through your use of the Platform and understand that such fees may change over time.

You agree not to use the Platform for any unlawful, harmful, or unauthorized purpose. This includes, but is not limited to, engaging in fraudulent activities, attempting to gain unauthorized access to other accounts or systems, and violating any applicable laws or regulations. We reserve the right to suspend or terminate your account if we suspect or determine that you have violated these Terms or misused the Platform.

All intellectual property on the Platform—including but not limited to software, design, text, graphics, logos, and trademarks—is owned by BrokerX or its licensors. You may not copy, reproduce, distribute, or exploit any part of the Platform without our prior written permission.

Your use of the Platform is also subject to our Privacy Policy, which outlines how we collect, use, and protect your personal information. By agreeing to these Terms, you also consent to the practices described in the Privacy Policy.

We may provide links to third-party services, content, or websites. These third-party services are not under our control, and we are not responsible for their content, accuracy, or practices. Your use of any third-party services is at your own risk and subject to their respective terms.

To the fullest extent permitted by applicable law, BrokerX disclaims all warranties, express or implied, regarding the Platform and its services. We shall not be liable for any indirect, incidental, special, or consequential damages arising out of or in connection with your use of the Platform, including but not limited to lost profits, trading losses, or data loss.

You agree to indemnify and hold harmless BrokerX, its officers, directors, employees, affiliates, and agents from any claims, damages, liabilities, and expenses (including legal fees) arising out of your use of the Platform, your violation of these Terms, or your violation of any rights of a third party.

These Terms shall be governed by and construed in accordance with the laws of Malaysia, without regard to its conflict of laws provisions. Any disputes arising from or related to these Terms or your use of the Platform shall be resolved exclusively in the courts of Selangor, Malaysia.

We may update or revise these Terms from time to time to reflect changes in our services or applicable laws. When we do, we will post the updated Terms within the Platform. Your continued use of the Platform after such changes constitutes your acceptance of the updated Terms.

If you have any questions about these Terms, please contact us at:
Email: support@brokerapp.com
Phone: +60 12-345 6789
Address: BrokerX Technologies Sdn. Bhd., Level 12, Menara BrokerX, Jalan Teknologi, Petaling Jaya, Selangor, Malaysia

By using this Platform, you acknowledge that you have read, understood, and agreed to these Terms and Conditions.
""".trimIndent()

