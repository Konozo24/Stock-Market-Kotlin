package com.example.brokerx

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.brokerx.viewmodels.AuthState
import com.example.brokerx.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun SplashRouter(
    navController: NavController,
    authState: AuthState?,
    kycCompleted: Boolean?,
    firebaseUser: FirebaseUser?,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    LaunchedEffect(authState, kycCompleted, firebaseUser) {
        Log.d("SplashRouter", "authState = $authState, kycCompleted = $kycCompleted, user = $firebaseUser")

        when (authState) {
            is AuthState.Loading -> {
                Log.d("SplashRouter", "authState is null, waiting...")
            }
            is AuthState.Unauthenticated -> {
                navController.navigate("Login") {
                    popUpTo("Splash") { inclusive = true }
                }
                return@LaunchedEffect // ✅ exit early
            }


            is AuthState.Authenticated -> {
                if (kycCompleted == null) return@LaunchedEffect // ✅ wait for DataStore
                if (kycCompleted == true) {
                    navController.navigate("WatchList") {
                        popUpTo("Splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("KycPage") {
                        popUpTo("Splash") { inclusive = true }
                    }
                }

            }
            is AuthState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_SHORT).show()
                authViewModel.clearAuthState()
                navController.navigate("Login") {
                    popUpTo("Splash") { inclusive = true }
                }
            }

            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}
