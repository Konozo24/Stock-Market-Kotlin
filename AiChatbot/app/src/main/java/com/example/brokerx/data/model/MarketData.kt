package com.example.brokerx.data.model

import com.google.gson.annotations.SerializedName

data class MarketData(
    @SerializedName("current_price") val currentPrice: Map<String, Double>,
    @SerializedName("total_volume") val totalVolume: Map<String, Double>
)
