package com.example.brokerx.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AssetDetail(
    val coinId: String,
    val symbol: String,
    val name: String,            // Works for companyName OR cryptoName
    val price: Double,
    val change: Double,
    val volume: Double,
    val latestTradingDay: String,
    val history: List<Double>,   // Prices for chart
    val historyTime: List<String> // Corresponding timestamps/dates
) : Parcelable
