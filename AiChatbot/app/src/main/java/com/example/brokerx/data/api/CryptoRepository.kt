package com.example.brokerx.data.api

import com.example.brokerx.data.api.RetrofitInstance.coinGeckoApi
import com.example.brokerx.data.local.MarketDao
import com.example.brokerx.data.local.MarketEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class CryptoRepository(
    private val marketDao: MarketDao,
){

    suspend fun getChartData(
        coinId: String,
        days: String = "1",              // default = 1 day
        interval: String = "minutely"    // default = minutely (live-like chart)
    ): Pair<List<Float>, List<String>> {
        val response = RetrofitInstance.coinGeckoApi.getMarketChart(
            coinId = coinId,
            vsCurrency = "usd",
            days = days,
            interval = interval,
            apiKey = Constants.coinGeckoApi
        )

        val prices = response.prices.map { it[1].toFloat() }
        val times = response.prices.map { timestampPrice ->
            val ts = timestampPrice[0].toLong()
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
        }

        return Pair(prices, times)
    }

    /**
     * Get markets with caching:
     * - Return from Room if available
     * - Otherwise fetch from API and save to Room
     */
    suspend fun getMarkets(ids: List<String>): List<MarketEntity> {
        // 1️⃣ Get cached coins that match requested IDs
        val cached = marketDao.getMarketsByIds(ids)
        val cachedIds = cached.map { it.id }

        // 2️⃣ Determine which IDs are missing
        val missingIds = ids.filter { it !in cachedIds }

        val apiEntities = if (missingIds.isNotEmpty()) {
            val response = coinGeckoApi.getMarkets(
                vsCurrency = "usd",
                ids = missingIds.joinToString(","),
                sparkline = true,
                apiKey = Constants.coinGeckoApi
            )

            val entities = response.map {
                MarketEntity(
                    id = it.id,
                    symbol = it.symbol.uppercase(),
                    name = it.id,
                    price = it.currentPrice,
                    change = it.priceChangePercentage24h,
                    history = it.sparkline_in_7d?.price ?: emptyList(),
                    historyTime = emptyList(),
                    volume = it.totalVolume,
                    logoUrl = it.image
                )
            }

            // Merge with existing cache instead of just inserting
            val existing = marketDao.getAllMarkets().associateBy { it.id }
            val merged = (existing + entities.associateBy { it.id }).values.toList()
            marketDao.insertMarkets(merged)

            entities
        } else emptyList()

        // 3️⃣ Return cached + newly fetched
        return cached + apiEntities
    }

    suspend fun getMarketsFromApi(ids: List<String>): List<MarketEntity> {
        val response = coinGeckoApi.getMarkets(
            vsCurrency = "usd",
            ids = ids.joinToString(","),
            sparkline = true,
            apiKey = Constants.coinGeckoApi
        )

        val entities = response.map {
            MarketEntity(
                id = it.id,
                symbol = it.symbol.uppercase(),
                name = it.id,
                price = it.currentPrice,
                change = it.priceChangePercentage24h,
                history = it.sparkline_in_7d?.price ?: emptyList(),
                historyTime = emptyList(),
                volume = it.totalVolume,
                logoUrl = it.image
            )
        }

        // Merge with existing cache instead of replacing
        val existing = marketDao.getAllMarkets().associateBy { it.id }
        val merged = (existing + entities.associateBy { it.id }).values.toList()
        marketDao.insertMarkets(merged)

        return entities
    }

    suspend fun getCachedMarkets(): List<MarketEntity> {
        return marketDao.getAllMarkets()
    }

    /**
     * Force refresh from API (e.g. user presses refresh).
     * - Always fetch from API
     * - Save results into Room
     * - Return new data
     */

    suspend fun refreshMarkets(ids: List<String>): List<MarketEntity> {
        val response = coinGeckoApi.getMarkets(
            vsCurrency = "usd",
            ids = ids.joinToString(","),
            sparkline = true,
            apiKey = Constants.coinGeckoApi
        )

        val entities = response.map {
            MarketEntity(
                id = it.id,
                symbol = it.symbol.uppercase(),
                name = it.id,
                price = it.currentPrice,
                change = it.priceChangePercentage24h,
                history = it.sparkline_in_7d?.price ?: emptyList(),
                historyTime = emptyList(), // optional: fill later
                volume = it.totalVolume,
                logoUrl = it.image
            )
        }

        // Merge with existing cache instead of clearing
        val existing = marketDao.getAllMarkets().associateBy { it.id }
        val merged = (existing + entities.associateBy { it.id }).values.toList()
        marketDao.insertMarkets(merged)

        return entities
    }

    suspend fun getAllMarkets(page: Int = 1): List<MarketEntity> {
        val allEntities = mutableListOf<MarketEntity>()
        var currentPage = page
        var fetched: List<MarketEntity>

        do {
            try {
                val response = coinGeckoApi.getAllMarkets(vsCurrency = "usd", page = currentPage, apiKey = Constants.coinGeckoApi)
                Log.d("MarketsAPI", "Page $currentPage: fetched ${response.size} markets")

                fetched = response.map { marketResponse ->
                    MarketEntity(
                        id = marketResponse.id,
                        symbol = marketResponse.symbol.uppercase(),
                        name = marketResponse.id,
                        price = marketResponse.currentPrice,
                        change = marketResponse.priceChangePercentage24h,
                        history = marketResponse.sparkline_in_7d?.price ?: emptyList(),
                        historyTime = emptyList(),
                        volume = marketResponse.totalVolume,
                        logoUrl = marketResponse.image
                    )
                }
            } catch (e: Exception) {
                Log.e("MarketsAPI", "Failed fetching page $currentPage: ${e.message}")
                fetched = emptyList()
            }

            allEntities.addAll(fetched)
            currentPage++
        } while (fetched.isNotEmpty())

        return allEntities
    }

    /**
     * Fetch all markets page by page and emit incrementally as Flow
     */
    fun getAllMarketsFlow(): Flow<List<MarketEntity>> = flow {
        val allEntities = mutableListOf<MarketEntity>()
        var currentPage = 1
        var fetched: List<MarketEntity>

        do {
            try {
                val response = coinGeckoApi.getAllMarkets(
                    vsCurrency = "usd",
                    page = currentPage,
                    apiKey = Constants.coinGeckoApi
                )
                Log.d("MarketsAPI", "Page $currentPage: fetched ${response.size} markets")

                fetched = response.map { marketResponse ->
                    MarketEntity(
                        id = marketResponse.id,
                        symbol = marketResponse.symbol.uppercase(),
                        name = marketResponse.id,
                        price = marketResponse.currentPrice,
                        change = marketResponse.priceChangePercentage24h,
                        history = marketResponse.sparkline_in_7d?.price ?: emptyList(),
                        historyTime = emptyList(),
                        volume = marketResponse.totalVolume,
                        logoUrl = marketResponse.image
                    )
                }
            } catch (e: Exception) {
                Log.e("MarketsAPI", "Failed fetching page $currentPage: ${e.message}")
                fetched = emptyList()
            }

            allEntities.addAll(fetched)
            emit(allEntities) // emit cumulative results after each page
            currentPage++
        } while (fetched.isNotEmpty())
    }

    suspend fun getTopMarkets(limit: Int = 50, forceRefresh: Boolean = false): List<MarketEntity> {
        if (!forceRefresh) {
            val cached = marketDao.getAllMarkets()
            if (cached.isNotEmpty()) return cached.take(limit)
        }

        return try {
            val response = coinGeckoApi.getAllMarkets(
                vsCurrency = "usd",
                page = 1,
                perPage = limit,
                apiKey = Constants.coinGeckoApi
            )

            val entities = response.map {
                MarketEntity(
                    id = it.id,
                    symbol = it.symbol.uppercase(),
                    name = it.id,
                    price = it.currentPrice,
                    change = it.priceChangePercentage24h,
                    history = it.sparkline_in_7d?.price ?: emptyList(),
                    historyTime = emptyList(),
                    volume = it.totalVolume,
                    logoUrl = it.image
                )
            }

            // Merge with existing cache instead of clearing
            val existing = marketDao.getAllMarkets().associateBy { it.id }
            val merged = (existing + entities.associateBy { it.id }).values.toList()
            marketDao.insertMarkets(merged)

            merged
        } catch (e: Exception) {
            Log.e("MarketsAPI", "Failed fetching top markets: ${e.message}")
            marketDao.getAllMarkets().take(limit)
        }
    }
}