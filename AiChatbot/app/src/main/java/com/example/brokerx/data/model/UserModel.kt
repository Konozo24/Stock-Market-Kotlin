package com.example.brokerx.data.model

data class UserModel(
    val email : String = "",
    val username : String = "",
    val uid : String = "",
    val cash: Double = 0.0,
    val watchlist: List<String> = emptyList(),
    val bankAccount: BankAccount? = null,

    // ✅ KYC details
    val fullName: String = "",
    val dob: String = "",          // "DD-MM-YYYY"
    val gender: String = "",
    val nationality: String = "",
    val address: String = "",
    val contactNumber: String = "",
    val kycCompleted: Boolean = false
)
