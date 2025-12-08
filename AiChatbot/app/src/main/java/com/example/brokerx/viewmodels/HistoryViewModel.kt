package com.example.brokerx.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.brokerx.data.model.AssetOrder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HistoryViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _orders = MutableLiveData<List<AssetOrder>>(emptyList())
    val orders: LiveData<List<AssetOrder>> = _orders

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var listenerRegistration: ListenerRegistration? = null

    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("No user logged in")

    init {
        // Listen for user changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchOrderHistory()
            } else {
                clearData()
            }
        }
    }

    /**
     * Fetch all orders for the current user from Firebase
     * Orders are sorted by timestamp in descending order (newest first)
     */
    fun fetchOrderHistory() {
        _isLoading.value = true
        _error.value = null

        // 🔑 remove previous listener if exists
        listenerRegistration?.remove()

        listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HistoryVM", "Error listening to orders", e)
                    _error.value = "Failed to load trading history"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ordersList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val order = doc.toObject(AssetOrder::class.java)
                            if (order != null) {
                                if (order.orderId.isBlank()) {
                                    order.orderId = doc.id
                                }
                            }
                            order
                        } catch (ex: Exception) {
                            Log.w("HistoryVM", "Failed to parse order ${doc.id}", ex)
                            null
                        }
                    }
                    _orders.value = ordersList
                    _isLoading.value = false
                    Log.d("HistoryVM", "Orders updated: ${ordersList.size} orders loaded")
                }
            }
    }

    /**
     * Refresh order history manually
     */
    fun refreshHistory() {
        fetchOrderHistory()
    }

    fun getOrdersByType(type: String): List<AssetOrder> {
        return _orders.value?.filter {
            it.type.equals(type, ignoreCase = true)
        } ?: emptyList()
    }

    fun getOrdersBySymbol(symbol: String): List<AssetOrder> {
        return _orders.value?.filter {
            it.symbol.equals(symbol, ignoreCase = true)
        } ?: emptyList()
    }

    fun getTotalTradingVolume(): Double {
        return _orders.value?.sumOf { it.price * it.quantity } ?: 0.0
    }

    fun getBuyOrdersCount(): Int {
        return _orders.value?.count {
            it.type.equals("BUY", ignoreCase = true)
        } ?: 0
    }

    fun getSellOrdersCount(): Int {
        return _orders.value?.count {
            it.type.equals("SELL", ignoreCase = true)
        } ?: 0
    }

    /**
     * Clear all data (called on logout)
     */
    fun clearData() {
        listenerRegistration?.remove()
        listenerRegistration = null
        _orders.value = emptyList()
        _isLoading.value = true
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // 🔑 cleanup when VM destroyed
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
