package com.example.sheloans.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sheloans.R
import com.example.sheloans.adapters.LoanAdapter
import com.example.sheloans.models.LoanProvider

class LoansActivity : AppCompatActivity() {

    private lateinit var loanList: ListView
    private lateinit var etOtherBank: EditText
    private lateinit var btnSearchBank: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loans)

        loanList = findViewById(R.id.loanList)
        etOtherBank = findViewById(R.id.etOtherBank)
        btnSearchBank = findViewById(R.id.btnSearchBank)

        val loans = listOf(
            LoanProvider(
                "Equity Bank", 
                "Up to KES 200k", 
                "12%", 
                "https://equitygroupholdings.com/ke/borrow/equitel-loans",
                "Fanikisha Women’s Loans for business growth."
            ),
            LoanProvider(
                "KCB Bank", 
                "Up to KES 500k", 
                "13%", 
                "https://ke.kcbgroup.com/borrow/loans/women-s-proposition",
                "FLME (Female Led & Owned Enterprises) credit."
            ),
            LoanProvider(
                "Absa Kenya", 
                "Up to KES 1M", 
                "13.5%", 
                "https://www.absabank.co.ke/personal/borrow/loans/women-banking/",
                "She Business Account with specialized financing."
            ),
            LoanProvider(
                "KWFT", 
                "Flexible", 
                "14%", 
                "https://www.kwftbank.com/products/business-loans/",
                "Kenya Women Microfinance Bank specializing in women."
            ),
            LoanProvider(
                "Family Bank",
                "Up to KES 300k", 
                "12.5%", 
                "https://familybank.co.ke/business/women-banking/",
                "Queen's Banking services for female entrepreneurs."
            )
        )

        val adapter = LoanAdapter(loans)
        loanList.adapter = adapter

        btnSearchBank.setOnClickListener {
            val bankName = etOtherBank.text.toString().trim()
            if (bankName.isNotEmpty()) {
                val query = "$bankName Kenya loan products for women"
                val url = "https://www.google.com/search?q=${Uri.encode(query)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a bank name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
