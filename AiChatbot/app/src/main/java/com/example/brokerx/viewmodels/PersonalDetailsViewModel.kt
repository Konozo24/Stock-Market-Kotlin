package com.example.brokerx.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore

class PersonalDetailsViewModel : ViewModel() {
    var username by mutableStateOf("")
    var contactNumber by mutableStateOf("")
    var email by mutableStateOf("")
    var isEditing by mutableStateOf(false)
    var hasLoaded by mutableStateOf(false)

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun loadUserDetails() {
        if (hasLoaded) return

        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                username = doc.getString("username") ?: ""
                contactNumber = doc.getString("contactNumber") ?: ""
                email = doc.getString("email") ?: user.email ?: ""
                hasLoaded = true //  prevent future reloads
            }
            .addOnFailureListener {
                Log.e("LoadUserDetails", "Failed to load user data", it)
            }
    }

    fun saveDetails(usernameInput: String, contactInput: String, emailInput: String, onReauthRequired: () -> Unit) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val updates = mapOf(
            "username" to usernameInput,
            "contactNumber" to contactInput,
            "email" to emailInput
        )

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Log.d("SaveDetails", "Firestore updated")
            }
            .addOnFailureListener {
                Log.e("SaveDetails", "Failed to update Firestore", it)
            }

        if (emailInput != user.email) {
            user.updateEmail(emailInput)
                .addOnSuccessListener {
                    Log.d("SaveDetails", "Email updated in Firebase Auth")
                }
                .addOnFailureListener { e ->
                    Log.e("SaveDetails", "Email update failed", e)
                    if (e is FirebaseAuthRecentLoginRequiredException) {
                        onReauthRequired()
                    }
                }
        }

        // Update local state
        username = usernameInput
        contactNumber = contactInput
        email = emailInput
    }

    fun reauthenticate(password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        user.reauthenticate(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
