package com.example.sheloans.utils

fun formatCurrency(amount: Double): String {
    return "KES %.2f".format(amount)
}

fun calculateSavingsProgress(saved: Double, target: Double): Int {
    return ((saved / target) * 100).toInt()
}