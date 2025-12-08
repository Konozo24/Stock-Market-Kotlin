package com.example.brokerx.data.model

import com.google.gson.annotations.SerializedName

data class CryptoDailyPrice(
    @SerializedName("1a. open (USD)")
    val open: String,

    @SerializedName("2a. high (USD)")
    val high: String,

    @SerializedName("3a. low (USD)")
    val low: String,

    @SerializedName("4a. close (USD)")
    val closeUsd: String,

    @SerializedName("5. volume")
    val volume: String,

    @SerializedName("6. market cap (USD)")
    val marketCap: String
)
