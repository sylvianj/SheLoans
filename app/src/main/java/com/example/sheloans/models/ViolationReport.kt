package com.example.sheloans.models

data class ViolationReport(
    val reportId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val issueType: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val status: String = "Pending" // Pending, Investigating, Resolved
)
