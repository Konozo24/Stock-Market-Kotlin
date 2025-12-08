package com.example.brokerx.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.brokerx.data.local.UserPreferences
import com.example.brokerx.data.local.UserPrefsSingleton
import com.example.brokerx.data.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState


    init {
        val user = FirebaseAuth.getInstance().currentUser
        _authState.value = if (user != null) {
            AuthState.Authenticated(user.uid)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun clearAuthState() {
        _authState.value = AuthState.Unauthenticated
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val user = snapshot.toObject(UserModel::class.java)
                        if (user != null) {
                            if (user.kycCompleted) {
                                _authState.value = AuthState.Authenticated(user.uid)
                            } else {
                                _authState.value = AuthState.RequiresKyc(user.uid) // ✅ new state
                            }
                        } else {
                            _authState.value = AuthState.Unauthenticated
                        }
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(context: Context, email : String, password : String){

        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId != null) {

                    // ✅ Check Firestore for KYC status
                    firestore.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val user = snapshot.toObject(UserModel::class.java)
                                if (user != null) {
                                    Log.d("LoginDebug", "Firestore kycCompleted = ${user.kycCompleted}")

                                    val userPrefs = UserPrefsSingleton.getInstance(context)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        userPrefs.saveKycCompleted(user.kycCompleted)
                                        userPrefs.saveUserId(userId)

                                        withContext(Dispatchers.Main) {
                                            Log.d("LoginDebug", "Setting authState based on kycCompleted = ${user.kycCompleted}")
                                            _authState.value = if (user.kycCompleted == true) {
                                                AuthState.Authenticated(userId)
                                            } else {
                                                AuthState.RequiresKyc(userId)
                                            }
                                        }
                                    }
                                } else {
                                    _authState.value = AuthState.Error("User data missing")
                                }
                            } else {
                                _authState.value = AuthState.Error("User record not found")
                            }
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error(e.message ?: "Failed to fetch user record")
                        }
                } else {
                    _authState.value = AuthState.Error("User ID not found")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
    }

    fun signup(email : String, password : String){

        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        val username = email.substringBefore("@")

                        val userModel = UserModel(
                            email,
                            username,
                            userId,
                            cash = 1000000.0,
                            watchlist = emptyList(),
                            bankAccount = null,
                            kycCompleted = false
                        )
                        Log.d("SignUpDebug", "Signup successful, posting RequiresKyc")
                        firestore.collection("users").document(userId)
                            .set(userModel)
                            .addOnSuccessListener {
                                _authState.postValue(AuthState.RequiresKyc(userId))
                            }
                            .addOnFailureListener { e ->
                                _authState.postValue(AuthState.Error(e.message ?: "Failed to create user"))
                            }
                    } else {
                        _authState.postValue(AuthState.Error("User ID is null"))

                    }

                } else {
                    _authState.postValue(AuthState.Error(task.exception?.message ?: "Something went wrong"))
                }
            }

    }

    fun signout(portfolioViewModel: PortfolioViewModel? = null, userPreferences: UserPreferences? = null){
        auth.signOut()
        _authState.postValue(AuthState.Unauthenticated)
        Log.d("AuthDebug", "Signout triggered, authState = Unauthenticated")
        // wipe portfolio/wallet cache
        portfolioViewModel?.clearData()

        // Clear DataStore
        userPreferences?.let {
            CoroutineScope(Dispatchers.IO).launch {
                it.saveUserId("")
            }
        }
    }

    fun resetPassword(email: String? = null) {
        val userEmail = email ?: auth.currentUser?.email

        if (userEmail.isNullOrEmpty()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(userEmail)
            .addOnSuccessListener {
                _authState.value = AuthState.PasswordResetSent
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
            }
    }

}

sealed class AuthState {
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class RequiresKyc(val userId: String) : AuthState()
    data class Error(val message : String) : AuthState()
    object PasswordResetSent : AuthState()
}