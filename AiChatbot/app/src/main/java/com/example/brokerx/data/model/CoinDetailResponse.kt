package com.example.brokerx.data.model

import com.google.gson.annotations.SerializedName

data class CoinDetailResponse(
    val id: String,
    val symbol: String,
    val name: String,
    @SerializedName("market_data") val marketData: MarketData
)
