package com.example.sheloans.utils

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    fun formatKes(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        // NumberFormat.getCurrencyInstance(Locale("en", "KE")) often returns "Ksh" or "KES" 
        // depending on the Android version. Let's ensure a consistent "KES" prefix.
        val formatted = format.format(amount)
        return formatted.replace("Ksh", "KES").replace("KES", "KES ")
    }
}
