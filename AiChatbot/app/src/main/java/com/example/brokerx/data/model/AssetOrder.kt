package com.example.brokerx.data.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class AssetOrder(
    var orderId: String = "",          // default value
    var symbol: String = "",           // default value
    var type: String = "",             // default value
    var quantity: Int = 0,             // default value
    var price: Double = 0.0,           // default value
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable


