package com.example.brokerx.data.local

import android.content.Context

object UserPrefsSingleton {
    private var INSTANCE: UserPreferences? = null

    fun getInstance(context: Context): UserPreferences {
        return INSTANCE ?: synchronized(this) {
            val instance = UserPreferences(context.applicationContext)
            INSTANCE = instance
            instance
        }
    }
}