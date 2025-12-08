package com.example.brokerx

import CryptoOrderPage
import CryptoOrderViewModel
import CryptoOrderViewModelFactory
import MarketsScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brokerx.Account.AccountScreen
import com.example.brokerx.Account.BankAccountScreen
import com.example.brokerx.Account.NotificationSettingsScreen
import com.example.brokerx.Account.PersonalDetailsScreen
import com.example.brokerx.Account.SettingsScreen
import com.example.brokerx.Account.TermsConditionScreen


import com.example.brokerx.authentication.LoginPage
import com.example.brokerx.authentication.SignUpPage
import com.example.brokerx.data.local.UserPrefsSingleton
import com.example.brokerx.data.model.AssetDetail
import com.example.brokerx.page.KycPage
import com.example.brokerx.screens.DepositScreen
import com.example.brokerx.screens.HistoryScreen
import com.example.brokerx.screens.PrivacyScreen
import com.example.brokerx.screens.WithdrawScreen
import com.example.brokerx.viewmodel.PersonalDetailsViewModel
import com.example.brokerx.viewmodels.AuthState
import com.example.brokerx.viewmodels.AuthViewModel
import com.example.brokerx.viewmodels.ChatViewModel
import com.example.brokerx.viewmodels.CryptoInfoViewModel
import com.example.brokerx.viewmodels.CryptoViewModel
import com.example.brokerx.viewmodels.HistoryViewModel
import com.example.brokerx.viewmodels.PortfolioViewModel
import com.example.brokerx.viewmodels.PortfolioViewModelFactory
import com.example.myapplication.page.ForgotPasswordPage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StockAppNavigation(
    chatViewModel: ChatViewModel,
    cryptoViewModel: CryptoViewModel,
    cryptoInfoViewModel: CryptoInfoViewModel,
    authViewModel: AuthViewModel,
    historyViewModel: HistoryViewModel,
    personalDetailsViewModel: PersonalDetailsViewModel
) {
    val navController = rememberNavController()

    val userPreferences = UserPrefsSingleton.getInstance(LocalContext.current)

    val lastUserId by userPreferences.lastUserId.collectAsState(initial = null)
    val kycCompleted by userPreferences.kycCompleted.collectAsState(initial = null)
    val authState by authViewModel.authState.observeAsState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser



    NavHost(
        navController = navController,
        startDestination = "Splash",
        route = "root"
    ) {


        // ✅ SplashRouter handles routing once
        composable("Splash") {
            SplashRouter(
                navController = navController,
                authState = authState,
                kycCompleted = kycCompleted,
                firebaseUser = firebaseUser,
                authViewModel = authViewModel
            )
        }

        composable("Login") {
            LoginPage(navController, authViewModel)
        }

        composable("SignUp") {
            SignUpPage(navController, authViewModel)
        }

        composable("ForgotPassword") {
            ForgotPasswordPage(navController = navController, authViewModel = authViewModel)
        }

        composable("KycPage") {
            KycPage(
                modifier = Modifier,
                navController = navController
            )
        }

        composable("WatchList") {backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            portfolioViewModel?.let {
                WatchList(
                    navController = navController,
                    stockViewModel = cryptoViewModel,
                    cryptoInfoViewModel = cryptoInfoViewModel,
                    authViewModel = authViewModel,
                    portfolioViewModel = it
                )
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please log in to view your watchlist", color = Color.White)
            }
        }

        composable("AiChatBot") {
            ChatPage(modifier = Modifier, chatViewModel, navController)
        }

        composable("AccountScreen") {backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            AccountScreen(authViewModel, portfolioViewModel, navController)
        }

        composable("SettingsScreen") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate("NotificationSettingsScreen") },
                onPrivacyClick = { navController.navigate("PrivacyScreen") },
                onTermsClick = { navController.navigate("TermsConditionScreen") }
            )
        }

        composable("BankAccountScreen") { backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            BankAccountScreen(
                navController = navController,
                portfolioVM = portfolioViewModel // or whatever your instance is
            )
        }

        composable("TermsConditionScreen") {
            TermsConditionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("PrivacyScreen"){
            PrivacyScreen (
                onBack = { navController.popBackStack() }
            )
        }

        composable("NotificationSettingsScreen") {
            NotificationSettingsScreen (
                onBack = { navController.popBackStack() }
            )
        }

        composable("PersonalDetailsScreen") {
            PersonalDetailsScreen(
                personalDetailsViewModel = personalDetailsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("CryptoInfo/{symbol}") { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            CryptoInfoPage(
                symbol = symbol,
                cryptoInfoViewModel = cryptoInfoViewModel,
                navController = navController
            )
        }

        composable("CryptoOrderPage") {backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            val cryptoDetail = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<AssetDetail>("cryptoDetail")
            Log.d("NavigationTest", "cryptoDetail in composable: $cryptoDetail")

            if (cryptoDetail != null && portfolioViewModel != null) {
                // ✅ Build CryptoOrderVM with factory
                val factory = CryptoOrderViewModelFactory(
                    firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                    auth = FirebaseAuth.getInstance(),
                    cryptoDetail = cryptoDetail,
                    portfolioVM = portfolioViewModel
                )

                val cryptoOrderViewModel: CryptoOrderViewModel = viewModel(factory = factory)

                CryptoOrderPage(
                    cryptoDetail = cryptoDetail,
                    cryptoVM = cryptoViewModel,
                    portfolioVM = portfolioViewModel,
                    cryptoOrderVM = cryptoOrderViewModel,
                    onBackToWatchlist = { navController.popBackStack() }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", color = Color.White)
                }
            }
        }

        composable("PortfolioPage") {backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            portfolioViewModel?.let {
                PortfolioScreen(portfolioViewModel = it, navController)
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please log in to view your portfolio", color = Color.White)
            }
        }

        composable("DepositScreen") { backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            portfolioViewModel?.let {
                DepositScreen(
                    portfolioViewModel = it,
                    navController = navController
                )
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please log in to deposit funds", color = Color.White)
            }
        }

        composable("HistoryScreen") {
            HistoryScreen(
                historyViewModel = historyViewModel, // You'll need to provide this
                navController = navController
            )
        }

        composable("WithdrawScreen") { backStackEntry ->
            val rootEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val portfolioViewModel: PortfolioViewModel = viewModel(
                rootEntry,
                factory = PortfolioViewModelFactory(cryptoViewModel, rootEntry)
            )

            portfolioViewModel.let {
                WithdrawScreen(
                    portfolioViewModel = it,
                    navController = navController
                )
            }
        }

        composable("MarketsScreen") {
            MarketsScreen(
                cryptoViewModel = cryptoViewModel,
                cryptoInfoViewModel = cryptoInfoViewModel,
                navController = navController
            )
        }

    }
}
