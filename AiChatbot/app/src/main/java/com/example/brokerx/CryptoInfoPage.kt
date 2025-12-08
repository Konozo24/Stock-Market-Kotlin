package com.example.brokerx

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.data.model.AssetDetail
import com.example.brokerx.data.model.AssetItem
import android.content.res.Configuration
import android.util.Log
import androidx.compose.ui.platform.LocalConfiguration
import com.example.brokerx.ui.theme.TradingViewChart
import androidx.compose.runtime.collectAsState
import com.example.brokerx.viewmodels.CryptoInfoViewModel


@Composable
fun CryptoInfoPage (
    symbol : String,
    cryptoInfoViewModel: CryptoInfoViewModel,
    navController: NavController,
    onBackClick: () -> Unit = {},
    onBuySellClick: () -> Unit = {}
    ) {
    val cryptoDetail  by cryptoInfoViewModel.cryptoDetail
    val isLoading by cryptoInfoViewModel.isLoading
    val errorMessage by cryptoInfoViewModel.errorMessage

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        errorMessage != null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = errorMessage ?: "Error", color = Color.Red)
        }

        cryptoDetail  != null -> {
            val detail = cryptoDetail!!

            if (isLandscape){
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    // Left: Chart (scrollable if needed)

                    Column (
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(end = 8.dp)
                    ){
                        CryptoInfoHeader(
                            symbol = detail.symbol,
                            companyName = detail.coinId,
                            navController = navController,
                            cryptoInfoViewModel = cryptoInfoViewModel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CryptoChartSection(
                            AssetItem(
                                coinId = detail.coinId,
                                symbol = detail.symbol,
                                price = detail.price,
                                change = detail.change,
                                volume = detail.volume,
                                history = detail.history,
                                historyTime = detail.historyTime
                            )
                        )
                    }

                    // Right: Stats + Button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        CryptoMarketStats(detail)
                        Spacer(modifier = Modifier.height(16.dp))
                        BuySellButton(navController, detail)
                    }
                }
            } else {
                // PORTRAIT: Normal scroll

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ){
                    CryptoInfoHeader(
                        symbol = detail.symbol,
                        companyName = detail.coinId,
                        navController = navController,
                        cryptoInfoViewModel = cryptoInfoViewModel
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    CryptoChartSection(
                        AssetItem(
                            coinId = detail.coinId,
                            symbol = detail.symbol,
                            price = detail.price,
                            change = detail.change,
                            volume = detail.volume,
                            history = detail.history,
                            historyTime = detail.historyTime
                        )
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    CryptoMarketStats(detail)
                    Spacer(modifier = Modifier.height(16.dp))
                    BuySellButton(navController, detail)
                }
            }
        }

        else -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data yet")
        }
    }
}

@Composable
fun CryptoInfoHeader(
    symbol: String,
    companyName: String,
    navController: NavController,
    cryptoInfoViewModel: CryptoInfoViewModel
) {
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    // Load favourite status when screen opens
    LaunchedEffect(companyName, orientation) {
        cryptoInfoViewModel.checkIfFavourite(companyName)
    }

    val isFavourite by cryptoInfoViewModel.isFavouriteState.collectAsState()
    val activeCoinId by cryptoInfoViewModel.activeCoinId.collectAsState()
    val showStar = activeCoinId == companyName && isFavourite



    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.clickable{ navController.popBackStack() })
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(symbol, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(companyName, fontSize = 14.sp, color = Color.Gray)
        }
        Icon(
            painter = painterResource( id = if (showStar) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24),
            tint = if (showStar) Color.Yellow else Color.White,
            contentDescription = "Favourite",
            modifier = Modifier.clickable{
                cryptoInfoViewModel.toggleFavourite(companyName)
            }
        )
    }
}

@Composable
fun CryptoChartSection (crypto: AssetItem) {

    val latestPrice = crypto.history.lastOrNull() ?: crypto.price
    val previousPrice = crypto.history.dropLast(1).lastOrNull() ?: latestPrice


    val priceColor = when {
        latestPrice > previousPrice -> Color(0xFF4CAF50) // Green
        latestPrice < previousPrice -> Color(0xFFF44336) // Red
        else -> Color.White // No change
    }

    // Horizontal scroll state
    val scrollState = rememberScrollState()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$${String.format("%.3f", crypto.price)}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "${if (crypto.change >= 0) "+" else ""}${String.format("%.2f", crypto.change)}%",
            color = if (crypto.change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        TradingViewChart(
            coinId = crypto.symbol,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // adjust as needed
        )

    }
}

fun formatVolume(volume: Double): String {
    return when {
        volume >= 1_000_000_000 -> String.format("%.2fB", volume / 1_000_000_000)
        volume >= 1_000_000 -> String.format("%.2fM", volume / 1_000_000)
        volume >= 1_000 -> String.format("%.2fK", volume / 1_000)
        else -> String.format("%.0f", volume)
    }
}

@Composable
fun CryptoMarketStats(detail: AssetDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Market Statistics",
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = Color.White
        )
        Divider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatRow(label = "Price", value = "${detail.price}")
            StatRow(label = "Change", value = "${String.format("%.2f", detail.change)}%")
        }
        StatRow(label = "Volume", formatVolume(detail.volume))

    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}


@Composable
fun BuySellButton(navController: NavController, cryptoDetail: AssetDetail) {
    Button(
        onClick = {
            // Save the cryptoDetail into the current back stack entry
            navController.currentBackStackEntry?.savedStateHandle?.set("cryptoDetail", cryptoDetail)

            // Navigate to the CryptoOrderPage
            navController.navigate("CryptoOrderPage") {
                launchSingleTop = true  // Avoid multiple copies of the same destination
            }

            // Optional: Log for debugging
            Log.d("Navigation", "Navigating with cryptoDetail: $cryptoDetail")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Buy Or Sell", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

