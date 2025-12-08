package com.example.brokerx.viewmodels

import PortfolioStock
import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokerx.data.model.AssetOrder
import com.example.brokerx.data.model.UserModel
import com.example.brokerx.data.model.BankAccount
import com.example.brokerx.data.model.Wallet
import com.example.brokerx.viewmodels.CryptoViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PortfolioViewModel(
    private val cryptoVM: CryptoViewModel,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _portfolio = MutableLiveData<List<PortfolioStock>>(savedStateHandle["portfolio"] ?: emptyList())
    val portfolio: LiveData<List<PortfolioStock>> = _portfolio

    private val _wallet = MutableLiveData<Wallet?>(savedStateHandle["wallet"])
    val wallet: LiveData<Wallet?> = _wallet

    private val _bankAccount = MutableLiveData<BankAccount?>()
    val bankAccount: LiveData<BankAccount?> = _bankAccount

    val totalValue: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun update() {
            // Only calculate if data is actually loaded
            val stocksValue = _portfolio.value?.sumOf { it.quantity * it.currentPrice } ?: 0.0
            val cash = _wallet.value?.cash ?: 0.0
            value = stocksValue + cash
        }
        addSource(_portfolio) { update() }
        addSource(_wallet) { update() }
    }

    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("No user logged in")

    val totalPL: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun update() {
            val pl = _portfolio.value?.sumOf {
                (it.currentPrice - it.avgPurchasePrice) * it.quantity
            } ?: 0.0
            value = pl
        }
        addSource(_portfolio) { update() }

    }

    // Unrealized Profit/Loss (absolute $)
    val unrealizedPL: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun update() {
            val pl = _portfolio.value?.sumOf { (it.currentPrice - it.avgPurchasePrice) * it.quantity } ?: 0.0
            value = pl
        }
        addSource(_portfolio) { update() }
    }

    // Unrealized Profit/Loss (%) relative to cost basis
    val unrealizedPLPercent: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun update() {
            val invested = _portfolio.value?.sumOf { it.avgPurchasePrice * it.quantity } ?: 0.0
            val pl = _portfolio.value?.sumOf { (it.currentPrice - it.avgPurchasePrice) * it.quantity } ?: 0.0
            value = if (invested > 0) (pl / invested) * 100 else 0.0
        }
        addSource(_portfolio) { update() }

    }

    init {
        _wallet.observeForever { savedStateHandle["wallet"] = it }
        _portfolio.observeForever { savedStateHandle["portfolio"] = it }

        // Listen for user changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchWallet()
                fetchPortfolio()
                fetchBankAccount()
            } else {
                clearData()
            }
        }

        // Observe CryptoViewModel cryptos for live price updates
        viewModelScope.launch {
            snapshotFlow { cryptoVM.cryptos.toList() }
                .distinctUntilChanged()
                .collectLatest { trackLivePrices() }
        }


    }

    /** Call this after login/signup immediately */
    fun loadUserData() {
        fetchWallet()
        fetchPortfolio()
    }

    fun getHoldingForSymbol(symbol: String): PortfolioStock? {
        return portfolio.value?.find { it.symbol.equals(symbol, ignoreCase = true) }
    }

    // --- Fetch ---
    fun fetchPortfolio() {
        firestore.collection("users")
            .document(userId)
            .collection("portfolio")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PortfolioVM", "Error listening to portfolio", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val stocks = snapshot.documents.mapNotNull { it.toObject(PortfolioStock::class.java) }
                    _portfolio.value = stocks
                    Log.d("PortfolioVM", "Portfolio updated live: $stocks")
                }
            }
    }

    fun fetchWallet() {
        firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PortfolioVM", "Error listening to wallet", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val cash = snapshot.getDouble("cash") ?: 0.0
                    _wallet.value = Wallet(cash)
                    Log.d("PortfolioVM", "Wallet updated live: $cash")
                }
            }
    }

    fun fetchBankAccount() {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val account = snapshot.toObject(UserModel::class.java)?.bankAccount
                _bankAccount.value = account
                Log.d("PortfolioVM", "Bank account fetched: $account")
            }
            .addOnFailureListener {
                Log.e("PortfolioVM", "Failed to fetch bank account", it)
            }
    }

    fun saveBankAccount(account: BankAccount) {
        firestore.collection("users")
            .document(userId)
            .set(mapOf("bankAccount" to account), SetOptions.merge())
            .addOnSuccessListener {
                _bankAccount.value = account
                Log.d("PortfolioVM", "Bank account saved: $account")
            }
            .addOnFailureListener {
                Log.e("PortfolioVM", "Failed to save bank account", it)
            }
    }

    // --- Wallet ---
    fun updateWallet(amount: Double) {
        viewModelScope.launch {
            try {
                // Read current cash from LiveData if available, otherwise fetch from Firestore
                val currentCash = _wallet.value?.cash
                    ?: firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()
                        .getDouble("cash") ?: 0.0

                val newCash = currentCash + amount

                // Update Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("cash", newCash)
                    .await()

                // Update local LiveData so UI reacts immediately
                _wallet.value = Wallet(newCash)
                Log.d("PortfolioVM", "Wallet updated: $currentCash -> $newCash")

            } catch (e: Exception) {
                Log.e("PortfolioVM", "Failed to update wallet", e)
            }
        }
    }


    // --- Portfolio ---
    fun addOrUpdateStock(order: AssetOrder, currentPrice: Double) {
        val list = _portfolio.value?.toMutableList() ?: mutableListOf()
        val index = list.indexOfFirst { it.symbol == order.symbol }

        if (index >= 0) {
            val existing = list[index]
            val totalQuantity = existing.quantity + order.quantity
            val newAvgPrice = ((existing.avgPurchasePrice * existing.quantity) +
                    (currentPrice * order.quantity)) / totalQuantity

            list[index] = existing.copy(
                quantity = totalQuantity,
                avgPurchasePrice = newAvgPrice,
                currentPrice = currentPrice
            )
        } else {
            list.add(
                PortfolioStock(
                    symbol = order.symbol,
                    quantity = order.quantity,
                    currentPrice = currentPrice,
                    avgPurchasePrice = currentPrice
                )
            )
        }

        _portfolio.value = list.toList() // 🔹 new immutable list

        firestore.collection("users")
            .document(userId)
            .collection("portfolio")
            .document(order.symbol)
            .set(list.first { it.symbol == order.symbol })
    }

    fun removeOrUpdateStock(order: AssetOrder) {
        val list = _portfolio.value?.toMutableList() ?: mutableListOf()
        val index = list.indexOfFirst { it.symbol == order.symbol }

        if (index >= 0) {
            val existing = list[index]
            val newQty = existing.quantity - order.quantity

            if (newQty <= 0) {
                list.removeAt(index)
                firestore.collection("users")
                    .document(userId)
                    .collection("portfolio")
                    .document(order.symbol)
                    .delete()
            } else {
                val updated = existing.copy(quantity = newQty)
                list[index] = updated
                firestore.collection("users")
                    .document(userId)
                    .collection("portfolio")
                    .document(order.symbol)
                    .set(updated)
            }
        }

        _portfolio.value = list.toList() // 🔹 fresh list again
    }

    // --- Live Price Tracking ---
    fun trackLivePrices() {
        val currentPortfolio = _portfolio.value?.map { it.copy() }?.toMutableList() ?: mutableListOf()

        cryptoVM.cryptos.forEach { crypto ->
            val index = currentPortfolio.indexOfFirst { it.symbol.equals(crypto.symbol, true) }
            if (index >= 0) {
                val stock = currentPortfolio[index]
                if (stock.currentPrice != crypto.price) {
                    currentPortfolio[index] = stock.copy(currentPrice = crypto.price) // fresh copy per stock
                }
            }
        }


            _portfolio.value = currentPortfolio.toList() // 🔹 emit fresh immutable list

    }




    fun clearData() {
        _wallet.value = null
        _portfolio.value = emptyList()
    }

    fun deposit(amount: Double) {
        if (_bankAccount.value == null) {
            Log.e("PortfolioVM", "Deposit blocked: no bank account")
            return
        }

        if (amount <= 0) {
            Log.e("PortfolioVM", "Invalid deposit amount: $amount")
            return
        }

        updateWallet(amount)
    }

    fun transfer(amount: Double) {
        if (_bankAccount.value == null) {
            Log.e("PortfolioVM", "Deposit blocked: no bank account")
            return
        }

        if (amount <= 0) {
            Log.e("PortfolioVM", "Invalid transfer amount: $amount")
            return
        }

        viewModelScope.launch {
            try {
                val currentCash = _wallet.value?.cash
                    ?: firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()
                        .getDouble("cash") ?: 0.0

                if (amount > currentCash) {
                    Log.e("PortfolioVM", "Transfer failed: insufficient balance")
                    return@launch
                }

                val newCash = currentCash - amount

                firestore.collection("users")
                    .document(userId)
                    .update("cash", newCash)
                    .await()

                _wallet.value = Wallet(newCash)

                Log.d("PortfolioVM", "Transfer successful: -$amount, new balance: $newCash")

            } catch (e: Exception) {
                Log.e("PortfolioVM", "Failed to transfer funds", e)
            }
        }
    }


}
