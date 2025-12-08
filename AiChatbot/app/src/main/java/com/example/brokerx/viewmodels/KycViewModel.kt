package com.example.brokerx.viewmodel

import KycFormState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class KycViewModel : ViewModel() {

    private val _formState = MutableStateFlow(
        KycFormState(
            email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        )
    )
    val formState: StateFlow<KycFormState> = _formState

    // 🔹 Generic updater
    fun updateForm(update: KycFormState.() -> KycFormState) {
        _formState.value = _formState.value.update()
    }

    // 🔹 Validation
    val isFormValid: StateFlow<Boolean> = formState.map { state ->
        state.fullName.isNotBlank() &&
                isValidDate(state.dob) &&
                state.gender.isNotBlank() &&
                state.nationality.isNotBlank() &&
                state.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) &&
                state.contactNumber.matches(Regex("^\\d{10,12}$")) &&
                state.address.isNotBlank() &&
                state.idType.isNotBlank() &&
                state.idNumber.isNotBlank() &&
                state.occupation.isNotBlank() &&
                state.sourceOfFunds.isNotBlank() &&
                state.incomeBracket.isNotBlank() &&
                state.tin.isNotBlank() &&
                state.investmentExperience.isNotBlank() &&
                state.agreedToTerms
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // 🔹 Date validation with proper calendar rules
    fun isValidDate(date: String): Boolean {
        return try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(java.time.format.ResolverStyle.STRICT)
            java.time.LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }
}