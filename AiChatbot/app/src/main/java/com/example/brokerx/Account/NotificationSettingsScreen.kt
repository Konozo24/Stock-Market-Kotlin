package com.example.brokerx.Account

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brokerx.R
import com.example.brokerx.data.local.UserPreferences
import com.example.brokerx.data.local.UserPrefsSingleton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = UserPrefsSingleton.getInstance(context)

    val notificationsEnabled by userPrefs.notificationsEnabled.collectAsState(initial = true)
    val vibrationEnabled by userPrefs.vibrationEnabled.collectAsState(initial = true)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notification Settings",
                        color = accentColor,
                        fontSize = 24.sp,
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Trade Alerts",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            NotificationToggleRow(
                label = "Enable notifications when trades are executed",
                checked = notificationsEnabled,
                onCheckedChange = {
                    scope.launch {
                        userPrefs.saveNotificationsEnabled(it)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            NotificationToggleRow(
                label = "Enable vibration for trade alerts",
                checked = vibrationEnabled,
                onCheckedChange = {
                    scope.launch {
                        userPrefs.saveVibrationEnabled(it)
                    }
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = secondaryText,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                uncheckedThumbColor = secondaryText
            )
        )
    }
}

