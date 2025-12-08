import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.brokerx.viewmodels.PortfolioViewModel
import com.example.brokerx.data.model.AssetDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CryptoOrderViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cryptoDetail: AssetDetail,
    private val portfolioVM: PortfolioViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoOrderViewModel::class.java)) {
            return CryptoOrderViewModel(firestore, auth, cryptoDetail, portfolioVM) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}