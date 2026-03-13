package com.example.sheloans.models

data class Loan(
    val userId: String = "",
    val bankName: String = "",
    val loanAmount: Double = 0.0,
    val interest: Double = 0.0,
    val status: String = "Pending"
)