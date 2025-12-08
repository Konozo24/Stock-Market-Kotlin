package com.example.brokerx.data.model

import com.google.gson.annotations.SerializedName

data class MarketResponse(
    val id: String,
    val symbol: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double,
    @SerializedName("total_volume") val totalVolume: Double,
    val sparkline_in_7d: SparklineResponse?,
    val image: String
)
