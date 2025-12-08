package com.example.brokerx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.brokerx.data.api.CryptoRepository
import com.example.brokerx.data.local.MarketDatabase
import com.example.brokerx.viewmodel.PersonalDetailsViewModel
import com.example.brokerx.viewmodels.AuthViewModel
import com.example.brokerx.viewmodels.ChatViewModel
import com.example.brokerx.viewmodels.CryptoInfoViewModel

import com.example.brokerx.viewmodels.CryptoViewModel
import com.example.brokerx.viewmodels.CryptoViewModelFactory
import com.example.brokerx.viewmodels.HistoryViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    // --- Repositories ---
    private lateinit var repository: CryptoRepository

    // --- ViewModels ---
    private val cryptoViewModel: CryptoViewModel by viewModels {
        CryptoViewModelFactory(repository)
    }

    private val personalDetailsViewModel: PersonalDetailsViewModel by viewModels()


    private val cryptoInfoViewModel: CryptoInfoViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val historyViewModel: HistoryViewModel by viewModels()
    private val authViewModel : AuthViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // Create Repo instance
        val db = MarketDatabase.getDatabase(applicationContext)
        repository = CryptoRepository(db.marketDao())




        setContent {
            StockAppNavigation(
                chatViewModel = chatViewModel,
                cryptoViewModel = cryptoViewModel,
                cryptoInfoViewModel = cryptoInfoViewModel,
                authViewModel = authViewModel,
                historyViewModel = historyViewModel,
                personalDetailsViewModel = personalDetailsViewModel
            )

        }
    }
}

