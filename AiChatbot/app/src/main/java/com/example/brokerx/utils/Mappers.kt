package com.example.brokerx.utils

import com.example.brokerx.data.local.MarketEntity
import com.example.brokerx.data.model.AssetItem

fun MarketEntity.toAssetItem(): AssetItem {
    return AssetItem(
        coinId = this.id,        // assuming your MarketItem has `id`
        symbol = this.symbol,
        price = this.price,
        change = this.change,
        volume = this.volume,    // if you have it in MarketItem
        history = emptyList(),   // or map real chart data if available
        historyTime = emptyList(),
        logoUrl = this.logoUrl
    )
}

// 🔹 Mapper for lists
fun List<MarketEntity>.toAssetItems(): List<AssetItem> {
    return this.map { it.toAssetItem() }
}

