package com.example.brokerx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_table")
    suspend fun getAllMarkets(): List<MarketEntity>

    @Query("SELECT * FROM market_table WHERE id IN (:ids)")
    suspend fun getMarketsByIds(ids: List<String>): List<MarketEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarkets(markets: List<MarketEntity>)

    @Query("DELETE FROM market_table")
    suspend fun clearMarkets()
}