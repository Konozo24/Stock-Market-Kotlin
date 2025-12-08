package com.example.brokerx.data.model

data class MarketChartResponse(
    val prices: List<List<Double>> // [timestamp, price]
)
