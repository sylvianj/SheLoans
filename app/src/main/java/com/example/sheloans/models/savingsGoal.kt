package com.example.sheloans.models

data class SavingsGoal(
    val id: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val chamaId: String? = null, // null if personal
    val chamaName: String? = null
)