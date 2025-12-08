package com.example.brokerx.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.brokerx.R
import com.example.brokerx.data.local.UserPreferences
import com.example.brokerx.data.local.UserPrefsSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Helper for showing notifications.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "trade_channel"
    private const val CHANNEL_NAME = "Trade Notifications"

    fun showTradeNotification(
        context: Context,
        type: String,
        symbol: String,
        quantity: Int,
        price: Double
    ) {
        val userPrefs = UserPrefsSingleton.getInstance(context)

        CoroutineScope(Dispatchers.Main).launch {
            val enabled = userPrefs.notificationsEnabled.first() // suspend call
            Log.d("NotificationDebug", "notificationsEnabled = $enabled")
            if (!enabled) return@launch // 🚫 User disabled notifications

            // ✅ Continue with notification logic
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Order Executed: $type")
                .setContentText("$quantity $symbol at $${String.format("%.2f", price)} • ${System.currentTimeMillis()}\"")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()
            Log.d("NotificationDebug", "Triggering notification for $symbol x$quantity")
            val notificationId = UUID.randomUUID().hashCode()
            NotificationManagerCompat.from(context)
                .notify(notificationId, notification)
        }
    }

    /**
     * Composable to request notification permission (Android 13+).
     * Call this once at the top of a screen where you need notifications.
     */
    @Composable
    fun NotificationPermissionRequester() {
        val context = LocalContext.current
        var permissionGranted by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionGranted = isGranted
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                Log.d("NotificationDebug", "Permission granted = $granted")

                if (!granted) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionGranted = true
                }
            } else {
                // On Android < 13, permission always granted
                permissionGranted = true
            }
        }
    }
}
