package com.example.brokerx.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Wallet(
    var cash: Double = 0.0
): Parcelable