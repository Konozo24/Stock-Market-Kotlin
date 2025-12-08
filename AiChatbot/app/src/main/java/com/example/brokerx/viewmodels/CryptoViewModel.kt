package com.example.brokerx.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokerx.data.api.RetrofitInstance
import com.example.brokerx.data.api.Constants
import com.example.brokerx.data.api.CryptoRepository
import com.example.brokerx.data.local.MarketEntity
import com.example.brokerx.data.model.AssetItem
import com.example.brokerx.utils.toAssetItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CryptoViewModel(
    private val repository: CryptoRepository
) : ViewModel() {


    private val _cryptos = mutableStateListOf<AssetItem>()
    val cryptos: List<AssetItem> get() = _cryptos
    private val _watchlistCryptos = MutableLiveData<List<AssetItem>>(emptyList())
    val watchlistCryptos: LiveData<List<AssetItem>> = _watchlistCryptos

    var userWatchlist by mutableStateOf(listOf<String>()) // Stores current user's watchlist
        private set

    var isWatchlistLoading by mutableStateOf(false)
        private set

    init {
        loadAllCryptosOnce(limit = 50) // only called once per app run
    }

    fun refreshAllCryptos(limit: Int = 50) {
        viewModelScope.launch {
            try {
                val freshMarkets = repository.getTopMarkets(limit, forceRefresh = true)
                mergeIntoCryptos(freshMarkets.map { it.toAssetItem() })
                loadWatchlistCryptos(forceRefresh = true)
            } catch (e: Exception) {
                Log.e("CryptoVM", "Failed refreshAllCryptos: ${e.message}")
            }
        }
    }

    fun loadAllCryptosOnce(limit: Int = 50) {
        viewModelScope.launch {
            try {
                val freshMarkets = repository.getTopMarkets(limit, forceRefresh = true)
                mergeIntoCryptos(freshMarkets.map { it.toAssetItem() })
                loadWatchlistCryptos(forceRefresh = true)
            } catch (e: Exception) {
                Log.e("CryptoVM", "Failed refreshAllCryptos: ${e.message}")
            }
        }
    }

    /** Merge fresh items into _cryptos, replacing existing coinIds if present */
    private fun mergeIntoCryptos(freshItems: List<AssetItem>) {
        val updatedList = _cryptos.map { old ->
            freshItems.find { it.coinId == old.coinId } ?: old
        } + freshItems.filter { fresh ->
            _cryptos.none { it.coinId == fresh.coinId }
        }
        _cryptos.clear()
        _cryptos.addAll(updatedList.distinctBy { it.coinId })
    }

    // Update user's watchlist
    fun updateWatchlist(coinIds: List<String>) {
        userWatchlist = coinIds.distinctBy { it.lowercase() }
        loadWatchlistCryptos(forceRefresh = true) // fetch market data for watchlist coins
    }

    // Fetch market data only for watchlist coins
     fun loadWatchlistCryptos(forceRefresh: Boolean = false) {
        if (userWatchlist.isEmpty()) {
            _watchlistCryptos.postValue(emptyList())
            isWatchlistLoading = false
            return
        }

        isWatchlistLoading = true
        viewModelScope.launch {
            try {
                val markets: List<MarketEntity> = if (forceRefresh) {
                    // Fetch fresh data from CoinGecko API
                    repository.getMarketsFromApi(userWatchlist)
                } else {
                    // Use cached data if available
                    repository.getMarkets(userWatchlist)
                }

                val items = markets
                    .map { it.toAssetItem() }
                    .distinctBy { it.symbol.uppercase() }
                    .sortedBy { it.symbol }
                _watchlistCryptos.postValue(items)
                mergeIntoCryptos(items) // Merge watchlist coins into main list
                Log.d("WatchList", "Watchlist fetched: $items")
            } catch (e: Exception) {
                // fallback to cached data
                val cached = repository.getCachedMarkets()
                    .filter { it.id in userWatchlist }
                    .map { it.toAssetItem() }
                    .distinctBy { it.coinId }
                    .sortedBy { it.symbol }
                _watchlistCryptos.postValue(cached)
                Log.d("WatchList", "Used Cache Market")
            } finally {
                isWatchlistLoading = false
            }
        }
    }



    // Refresh all market cryptos and watchlist at the same time
    /*fun refreshAll() {
        viewModelScope.launch {
            try {
                repository.getAllMarketsFlow().collect { marketEntities ->
                    val assetItems = marketEntities.map { it.toAssetItem() }
                    _cryptos.value = assetItems
                    filterWatchlist()
                }
            } catch (e: Exception) {
                // fallback to cache
                val cached = repository.getCachedMarkets()
                val assetItems = cached.map { it.toAssetItem() }
                _cryptos.value = assetItems
                filterWatchlist()
            }
        }
    }

    fun loadAllCryptos() {
        Log.d("CryptoVM", "loadAllCryptos() called")
        viewModelScope.launch {
            repository.getAllMarketsFlow().collect { marketEntities ->
                _cryptos.value = marketEntities.map { it.toAssetItem() }
            }
        }

    }

    fun loadCachedCryptos() {
        viewModelScope.launch {
            viewModelScope.launch {
                val cached = repository.getCachedMarkets()
                val assetItems = cached.map { it.toAssetItem() }
                _cryptos.value = assetItems
                filterWatchlist()
            }
        }
    }

    fun refreshCryptos(symbols: List<String>) {
        viewModelScope.launch {
            val fresh = repository.refreshMarkets(symbols)
            val assetItems = fresh.map { it.toAssetItem() }

            // Replace old entries with updated ones
            val updatedList = _cryptos.value.map { old ->
                assetItems.find { it.coinId == old.coinId } ?: old
            } + assetItems.filter { freshItem ->
                _cryptos.value.none { it.coinId == freshItem.coinId }
            }

            _cryptos.value = updatedList.distinctBy { it.coinId }
            filterWatchlist()
        }
    }*/


    private val _isVisible = MutableLiveData(true)
    val isVisible: LiveData<Boolean> = _isVisible

    fun toggleVisibility() {
        _isVisible.value = _isVisible.value != true
    }
}

