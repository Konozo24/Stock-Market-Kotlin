package com.example.brokerx.Account

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.viewmodels.AuthViewModel
import com.example.brokerx.viewmodels.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.brokerx.NavigationBarSection
import com.example.brokerx.R

// Color scheme
val backgroundColor = Color(0xFF0A0A0A)
val surfaceColor = Color(0xFF141414)
val primaryText = Color.White
val secondaryText = Color(0xFF888888)
val greenColor = Color(0xFF00D4AA)
val redColor = Color(0xFFFF4444)
val accentColor = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var username by remember { mutableStateOf("User") }
    var email by remember { mutableStateOf(firebaseUser?.email ?: "") }

    LaunchedEffect(firebaseUser) {
        firebaseUser?.uid?.let { uid ->
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()
                username = doc.getString("username") ?: "User" //  use fullName
                email = doc.getString("email") ?: ""           //  get email from Firestore
            } catch (e: Exception) {
                Log.e("AccountScreen", "Error fetching username: ${e.message}")
            }
        }
    }

    if (isLandscape) {
        // LANDSCAPE -> Side Nav
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            NavigationBarSection(navController = navController) // left sidebar

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                AccountContent(username, email, navController, authViewModel, portfolioViewModel)
            }
        }
    } else {
        // PORTRAIT -> Bottom Nav
        Scaffold(
            containerColor = backgroundColor,
            bottomBar = { NavigationBarSection(navController = navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                AccountContent(username, email, navController, authViewModel, portfolioViewModel)
            }
        }
    }
}

@Composable
fun AccountContent(
    username: String,
    email: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel
) {
    // Header Section
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Manage your profile & settings",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Profile Section - More compact like watchlist style
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E2E2E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = username,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )

        Text(
            text = email,
            fontSize = 14.sp,
            color = secondaryText
        )
    }

    Divider(
        color = Color(0xFF2E2E2E),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 16.dp)
    )

    // Account Options - Keep only the original 3
    AccountOptionRow(
        iconVector = Icons.Default.Person,
        title = "Personal Details",
        subtitle = "Account Info & KYC"
    ) {
        navController.navigate("PersonalDetailsScreen")
    }

    AccountOptionRow(
        iconPainter = painterResource(R.drawable.baseline_account_balance_wallet_24),
        title = "Bank Account",
        subtitle = "Manage your bank accounts"
    ) {
        navController.navigate("BankAccountScreen")
    }

    AccountOptionRow(
        iconVector = Icons.Default.Settings,
        title = "Settings",
        subtitle = "Notifications & Privacy"
    ) {
        navController.navigate("SettingsScreen")
    }

    Spacer(modifier = Modifier.height(32.dp))


}

@Composable
fun AccountOptionRow(
    iconVector: ImageVector? = null,
    iconPainter: Painter? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = primaryText,
                    modifier = Modifier.size(24.dp)
                )
            } else if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = title,
                    tint = primaryText,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = secondaryText
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = secondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
