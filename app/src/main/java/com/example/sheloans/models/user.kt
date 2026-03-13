package com.example.sheloans.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val businessType: String = "",
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val creditScore: Int = 0,
    val profileImageUrl: String = ""
)
