package com.example.brokerx.data.api

import com.example.brokerx.data.model.CoinDetailResponse
import com.example.brokerx.data.model.MarketChartResponse
import com.example.brokerx.data.model.MarketResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") coinId: String,                 // e.g. "bitcoin"
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: String = "1",          // history range (1,7,30,90,365,max)
        @Query("interval") interval: String = "minutely",
        @Header("X-Cg-Demo-Api-Key") apiKey: String
    ): MarketChartResponse

    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String, // comma-separated list: "bitcoin,ethereum,solana"
        @Query("sparkline") sparkline: Boolean = true,
        @Header("X-Cg-Demo-Api-Key") apiKey: String
    ): List<MarketResponse>

    @GET("coins/markets")
    suspend fun getAllMarkets(
        @Query("vs_currency") vsCurrency: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("x_cg_demo_api_key") apiKey: String
    ): List<MarketResponse>

    @GET("coins/{id}")
    suspend fun getCoinDetail(
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
        @Query("sparkline") sparkline: Boolean = false,
        @Header("X-Cg-Demo-Api-Key") apiKey: String
    ): CoinDetailResponse
}