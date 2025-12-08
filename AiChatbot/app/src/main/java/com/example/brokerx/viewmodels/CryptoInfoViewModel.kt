package com.example.brokerx.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.brokerx.data.model.AssetDetail
import com.example.brokerx.data.model.AssetItem

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CryptoInfoViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    // Holds the current stock’s detailed info
    var cryptoDetail = mutableStateOf<AssetDetail?>(null)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set


    private val _isFavouriteState = MutableStateFlow(false)
    val isFavouriteState: StateFlow<Boolean> = _isFavouriteState.asStateFlow()

    private val _currentFavouriteCoinId = MutableStateFlow<String?>(null)
    private val _activeCoinId = MutableStateFlow<String?>(null)
    val activeCoinId: StateFlow<String?> = _activeCoinId



    fun loadCryptoDetail(crypto: AssetItem) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                // Simulate minute-level price history for the demo
                val now = System.currentTimeMillis()
                val history = crypto.history.toMutableList()
                val historyTime = crypto.historyTime.toMutableList()

                // Generate minute-level timestamps if empty
                if (historyTime.isEmpty()) {
                    val interval = 60 * 1000L // 1 minute
                    historyTime.clear()
                    history.forEachIndexed { index, _ ->
                        val timestamp = now - (history.size - 1 - index) * interval
                        historyTime.add(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)))
                    }
                }

                cryptoDetail.value = AssetDetail(
                    coinId = crypto.coinId,
                    symbol = crypto.symbol,
                    name = crypto.coinId.replaceFirstChar { it.uppercase() }, // demo: use symbol as name
                    price = crypto.price,
                    change = crypto.change,
                    volume = crypto.volume, // demo key cannot get volume
                    latestTradingDay = "", // optional
                    history = history,
                    historyTime = historyTime
                )
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to load crypto detail"
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Call this periodically to simulate live updates.
     * Add a new price point at the end every minute.
     */
    fun updateChartLive(newPrice: Double) {


        val detail = cryptoDetail.value ?: return

        val newHistory = detail.history.toMutableList()
        val newHistoryTime = detail.historyTime.toMutableList()

        newHistory.add(newPrice)

        // Add timestamp for the new price
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        newHistoryTime.add(timestamp)

        // Keep last N points only for display
        val maxPoints = 60 // last 60 minutes
        if (newHistory.size > maxPoints) {
            newHistory.removeAt(0)
            newHistoryTime.removeAt(0)
        }

        cryptoDetail.value = detail.copy(
            history = newHistory,
            historyTime = newHistoryTime,
            price = newPrice,
            change = ((newPrice - newHistory.first()) / newHistory.first()) * 100
        )
    }

    fun addToFavourites(cryptoSymbol: String) {
        _isFavouriteState.value = true

        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .update("watchlist", FieldValue.arrayUnion(cryptoSymbol))
            .addOnFailureListener { e -> Log.e("CryptoInfoVM", "Failed to add favourite: ${e.message}") }
    }


    fun removeFromFavourites(cryptoSymbol: String) {

        _isFavouriteState.value = false

        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .update("watchlist", FieldValue.arrayRemove(cryptoSymbol))
            .addOnFailureListener { e -> Log.e("CryptoInfoVM", "Failed to remove favourite: ${e.message}") }
    }


    fun checkIfFavourite(coinId: String) {
        val userId = auth.currentUser?.uid ?: return
        _activeCoinId.value = coinId

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val watchlist = document.get("watchlist") as? List<String> ?: emptyList()
                // Compare using coinId, not symbol
                _isFavouriteState.value = coinId in watchlist
            }
            .addOnFailureListener { e ->
                Log.e("CryptoInfoVM", "Failed to fetch favourites: ${e.message}")
            }
    }

    fun toggleFavourite(cryptoSymbol: String) {
        if (_isFavouriteState.value) removeFromFavourites(cryptoSymbol)
        else addToFavourites(cryptoSymbol)
    }


}