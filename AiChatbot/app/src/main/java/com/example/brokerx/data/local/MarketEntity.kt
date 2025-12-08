package com.example.brokerx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_table")
data class MarketEntity(
    @PrimaryKey val id: String,        // coinId (e.g. "bitcoin")
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val history: List<Double>,         // serialized below
    val historyTime: List<Long>,
    val volume: Double,
    val logoUrl: String? = null
)
