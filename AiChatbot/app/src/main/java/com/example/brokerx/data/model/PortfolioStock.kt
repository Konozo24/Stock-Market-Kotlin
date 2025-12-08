import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PortfolioStock(
    val symbol: String = "",
    var quantity: Int = 0,
    var currentPrice: Double = 0.0,
    var avgPurchasePrice: Double = 0.0
) : Parcelable {
    val unrealizedGain: Double
        get() = (currentPrice - avgPurchasePrice) * quantity

    val gainPercent: Double
        get() = if (avgPurchasePrice != 0.0) (currentPrice - avgPurchasePrice) / avgPurchasePrice * 100 else 0.0
}