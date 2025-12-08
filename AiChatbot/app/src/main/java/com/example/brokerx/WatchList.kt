package com.example.brokerx

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.data.local.UserPreferences
import com.example.brokerx.data.model.AssetItem
import com.example.brokerx.data.model.UserModel

import com.example.brokerx.ui.theme.LineChart
import com.example.brokerx.ui.theme.backgroundColor
import com.example.brokerx.viewmodels.AuthState
import com.example.brokerx.viewmodels.AuthViewModel
import com.example.brokerx.viewmodels.CryptoInfoViewModel
import com.example.brokerx.viewmodels.CryptoViewModel
import com.example.brokerx.viewmodels.PortfolioViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun WatchList (navController: NavController, stockViewModel: CryptoViewModel, cryptoInfoViewModel : CryptoInfoViewModel, authViewModel: AuthViewModel, portfolioViewModel: PortfolioViewModel) {
    WatchListScreen(navController, stockViewModel, cryptoInfoViewModel, authViewModel, portfolioViewModel)
}

@Composable
fun WatchListScreen(
    navController: NavController,
    cryptoViewModel: CryptoViewModel,
    cryptoInfoViewModel: CryptoInfoViewModel,
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("Login") {
                popUpTo("WatchList") { inclusive = true }
            }
        }
    }

    if (isLandscape) {
        // LANDSCAPE -> Side Nav
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            NavigationBarSection(navController = navController) // left sidebar

            Column(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
            ) {
                MenuHeader(navController, authViewModel, portfolioViewModel, cryptoViewModel)
                AssetsSection(cryptoViewModel, portfolioViewModel)
                StockSection(cryptoViewModel, navController, cryptoInfoViewModel, portfolioViewModel)
            }
        }
    } else {
        // PORTRAIT -> Bottom Nav
        Scaffold(
            containerColor = backgroundColor,
            bottomBar = { NavigationBarSection(navController = navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .background(backgroundColor)
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                MenuHeader(navController, authViewModel, portfolioViewModel, cryptoViewModel)
                AssetsSection(cryptoViewModel, portfolioViewModel)
                StockSection(cryptoViewModel, navController, cryptoInfoViewModel, portfolioViewModel)
            }
        }
    }
}


@Composable
fun MenuHeader (navController: NavController, authViewModel: AuthViewModel, portfolioViewModel: PortfolioViewModel, cryptoViewModel: CryptoViewModel) {

    var name by remember {mutableStateOf("")}
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        try {
            val doc = Firebase.firestore.collection("users").document(uid).get().await()

            // Load username
            name = doc.getString("username") ?: "User"

            // Load watchlist symbols
            val watchlistSymbols = doc.get("watchlist") as? List<String> ?: emptyList()
            cryptoViewModel.updateWatchlist(watchlistSymbols)

        } catch (e: Exception) {
            Log.e("WatchlistScreen", "Error loading user data: ${e.message}")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "BrokerX",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Welcome Back, $name",
                fontSize = 16.sp, // slightly smaller
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Row {
            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        authViewModel.signout(portfolioViewModel, userPreferences)
                    },
                painter = painterResource(id = R.drawable.baseline_exit_to_app_24),
                contentDescription = "Sign Out",
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        navController.navigate("AiChatBot")
                    },
                painter = painterResource(id = R.drawable.baseline_message_24),
                contentDescription = "AI Chatbot",
                tint = Color.White
            )
        }
    }

}

@Composable
fun AssetsSection(cryptoViewModel: CryptoViewModel, portfolioViewModel: PortfolioViewModel) {
    val totalAssets by portfolioViewModel.totalValue.observeAsState(0.0)
    val wallet by portfolioViewModel.wallet.observeAsState()
    val portfolio by portfolioViewModel.portfolio.observeAsState(emptyList())
    val isVisible by cryptoViewModel.isVisible.observeAsState(true)
    val unrealizedPL by portfolioViewModel.unrealizedPL.observeAsState(0.0)
    val unrealizedPLPercent by portfolioViewModel.unrealizedPLPercent.observeAsState(0.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total Assets (USD)", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    wallet == null -> {
                        Text("Loading...", fontSize = 20.sp, color = Color.Gray)
                    }
                    isVisible -> {
                        Text(
                            text = "${String.format("%.2f", totalAssets)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    else -> {
                        Text(
                            text = "****",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            cryptoViewModel.toggleVisibility()
                        },
                    painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                    contentDescription = "Show/Hide",
                    tint = Color.White
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("Unrealized P/L", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))

            if (wallet != null) {
                Text(
                    text = String.format(
                        "%s%.2f ( %s%.2f%% )",
                        if (unrealizedPL >= 0) "+" else "",
                        unrealizedPL,
                        if (unrealizedPLPercent >= 0) "+" else "",
                        unrealizedPLPercent
                    ),
                    color = if (unrealizedPL >= 0) Color(0xFF2E7D32) else Color.Red,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text("Loading...", fontSize = 15.sp, color = Color.Gray)
            }
        }
    }

    Divider(
        color = Color.White,
        thickness = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}
@Composable
fun StockSection (
    cryptoViewModel: CryptoViewModel,
    navController: NavController,
    cryptoInfoViewModel : CryptoInfoViewModel,
    portfolioViewModel: PortfolioViewModel) {

    val watchlistCryptos by cryptoViewModel.watchlistCryptos.observeAsState(emptyList())
    val isLoading by remember { derivedStateOf { cryptoViewModel.isWatchlistLoading } }
    val coroutineScope = rememberCoroutineScope()



    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Charts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // 🔄 Refresh Button
            Icon(
                painter = painterResource(id = R.drawable.baseline_refresh_24), // add this icon to your res/drawable
                contentDescription = "Refresh",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {

                        coroutineScope.launch {
                            cryptoViewModel.refreshAllCryptos(limit = 50)
                            portfolioViewModel.trackLivePrices()
                        }


                    }
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))


    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading watchlist...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        watchlistCryptos.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cryptos in your watchlist",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        else -> {
            Column {
                watchlistCryptos.forEach { crypto ->
                    StockRow(crypto, navController) {
                        cryptoInfoViewModel.loadCryptoDetail(crypto)
                        navController.navigate("CryptoInfo/${crypto.symbol}")
                    }
                    Divider(color = Color.Gray, thickness = 1.dp)
                }
            }
        }
    }

}

@Composable
fun StockRow(stock: AssetItem, navController : NavController, onClick: () -> Unit) {
    val changeColor = if (stock.change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = stock.symbol,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        LineChart (
            prices = stock.history.map { it.toFloat() },
            times = stock.historyTime.map { it.toString() },
            modifier = Modifier
                .width(80.dp)
                .height(30.dp)

        )

        Spacer(modifier = Modifier.width(15.dp))

        Column (
            horizontalAlignment = Alignment.End
        ){
            Text(
                text = "$${String.format("%.2f", stock.price)}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (stock.change >= 0) "+${String.format("%.2f", stock.change)}%"
                else "${String.format("%.2f", stock.change)}%",
                color = changeColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun NavigationBarSection(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // SIDE NAV
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .background(Color.Black)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavBarItem(R.drawable.baseline_home_filled_24, "Home") {
                navController.navigate("Watchlist")
            }
            NavBarItem(R.drawable.baseline_bar_chart_24, "Market") {
                navController.navigate("MarketsScreen")
            }
            NavBarItem(R.drawable.baseline_account_balance_wallet_24, "Wallet") {
                navController.navigate("PortfolioPage")
            }
            NavBarItem(R.drawable.baseline_account_circle_24, "Account") {
                navController.navigate("AccountScreen")
            }
        }
    } else {
        // BOTTOM NAV
        Divider(color = Color.Gray, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavBarItem(R.drawable.baseline_home_filled_24, "Home") {
                navController.navigate("Watchlist")
            }
            NavBarItem(R.drawable.baseline_bar_chart_24, "Market") {
                navController.navigate("MarketsScreen")
            }
            NavBarItem(R.drawable.baseline_account_balance_wallet_24, "Wallet") {
                navController.navigate("PortfolioPage")
            }
            NavBarItem(R.drawable.baseline_account_circle_24, "Account") {
                navController.navigate("AccountScreen")
            }
        }
    }
}


@Composable
fun NavBarItem(
    icon: Int,
    label: String,
    onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}