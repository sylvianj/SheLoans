package com.example.sheloans.utils

object CreditScoreUtils {

    fun calculateCreditScore(
        savingsCompleted: Int,
        chamaParticipation: Int,
        loanRepayment: Int
    ): Int {
        var score = 50 // base score
        score += savingsCompleted * 5
        score += chamaParticipation * 5
        score += loanRepayment * 10
        return if(score > 100) 100 else score
    }
}