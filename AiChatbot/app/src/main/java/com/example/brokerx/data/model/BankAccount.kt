package com.example.brokerx.data.model

data class BankAccount(
    val bankName: String = "",
    val accountHolder: String = "",  // optional, can reuse KYC full name
    val accountNumber: String = ""
)