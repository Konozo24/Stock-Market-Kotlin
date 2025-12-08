package com.example.brokerx.data.model

data class AssetItem(
    val coinId: String,             // <- for CoinGecko API
    val symbol: String,
    val price: Double,
    val change: Double,
    val volume : Double,
    val history: List<Double>,
    val historyTime: List<String>,
    val logoUrl: String? = null   // <- optional
)