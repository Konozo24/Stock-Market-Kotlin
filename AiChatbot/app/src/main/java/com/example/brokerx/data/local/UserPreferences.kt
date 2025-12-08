package com.example.brokerx.data.local
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore(name = "user_prefs")
class UserPreferences(private val context: Context) {
    private val dataStore = context.userDataStore

    private val LAST_USER_ID = stringPreferencesKey("last_user_id")
    private val KYC_COMPLETED = booleanPreferencesKey("kyc_completed")

    val lastUserId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LAST_USER_ID]
    }

    val kycCompleted: Flow<Boolean?> = dataStore.data.map { prefs ->
        val value = prefs[KYC_COMPLETED]
        Log.d("DataStoreDebug", "Read kycCompleted = $value")
        value
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[LAST_USER_ID] = userId
        }
    }

    suspend fun saveKycCompleted(completed: Boolean) {
        Log.d("DataStoreDebug", "Saving kycCompleted = $completed")
        dataStore.edit { prefs ->
            prefs[KYC_COMPLETED] = completed
        }
    }

    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true // default to true
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        Log.d("NotificationToggle", "Saving notificationsEnabled = $enabled")
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED] ?: true // default to true
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED] = enabled
        }
    }
}

