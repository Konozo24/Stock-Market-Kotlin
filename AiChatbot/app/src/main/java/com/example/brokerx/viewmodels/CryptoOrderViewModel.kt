import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokerx.data.model.AssetDetail
import com.example.brokerx.data.model.AssetOrder
import com.example.brokerx.data.model.OrderType
import com.example.brokerx.viewmodels.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CryptoOrderViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cryptoDetail: AssetDetail,
    private val portfolioVM: PortfolioViewModel
) : ViewModel() {

    var quantity: Int = 0 // <-- default value

    private val _orders = MutableLiveData<List<AssetOrder>>(emptyList())
    val orders: LiveData<List<AssetOrder>> = _orders

    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    init {
        fetchOrdersFromFirestore()
    }

    private fun fetchOrdersFromFirestore() {
        firestore.collection("users")
            .document(userId)
            .collection("orders")
            .get()
            .addOnSuccessListener { result ->
                val orderList = result.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(AssetOrder::class.java)
                    } catch (e: Exception) {
                        Log.e("CryptoOrderVM", "Failed to deserialize order: ${doc.id}", e)
                        null
                    }
                }
                _orders.value = orderList
            }
            .addOnFailureListener { e ->
                Log.e("CryptoOrderVM", "Failed to fetch orders", e)
            }
    }

    fun placeOrder(type: OrderType, quantity: Int) : Boolean {

        val wallet = portfolioVM.wallet.value
        if (wallet == null) {
            Log.e("CryptoOrderVM", "Wallet not loaded yet")
            return false
        }

        val cost = cryptoDetail.price * quantity

        if (type == OrderType.BUY && wallet.cash < cost) {
            Log.e("CryptoOrderVM", "Not enough cash! Available: ${wallet.cash}, required: $cost")
            return false
        }

        if (type == OrderType.SELL) {
            val currentHolding = portfolioVM.getHoldingForSymbol(cryptoDetail.symbol)
            val ownedQuantity = currentHolding?.quantity ?: 0

            if (ownedQuantity < quantity) {
                Log.e("CryptoOrderVM", "Not enough crypto to sell! Owned: $ownedQuantity, trying to sell: $quantity")
                return false
            }
        }

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("orders")
            .document() // Firestore generates unique ID
        val orderId = docRef.id

        // Create order
        val order = AssetOrder(
            orderId = orderId,
            symbol = cryptoDetail.symbol,
            type = type.name, // Firestore can store this as String
            quantity = quantity,
            price = cryptoDetail.price,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                docRef.set(order).await()

                val currentOrders = _orders.value?.toMutableList() ?: mutableListOf()
                currentOrders.add(order)
                _orders.postValue(currentOrders)

                // 🔑 Deduct or add wallet balance AFTER order is stored
                if (type == OrderType.BUY) {
                    portfolioVM.updateWallet(-cost)
                    portfolioVM.addOrUpdateStock(order, cryptoDetail.price)
                } else {
                    val gain = order.price * order.quantity
                    portfolioVM.updateWallet(gain)
                    portfolioVM.removeOrUpdateStock(order)
                }
                portfolioVM.trackLivePrices()
                Log.d("CryptoOrderVM", "Order placed successfully: $order")
            } catch (e: Exception){
                Log.e("CryptoOrderVM", "Failed to place order", e)
            }

        }
        return true
    }

    fun clearData() {
        _orders.value = emptyList()
        quantity = 0
    }
}
