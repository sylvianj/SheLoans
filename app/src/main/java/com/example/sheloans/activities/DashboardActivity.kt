package com.example.sheloans.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sheloans.R
import com.example.sheloans.models.GroqMessage
import com.example.sheloans.models.GroqRequest
import com.example.sheloans.utils.CurrencyUtils
import com.example.sheloans.utils.GroqClient
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userNameText: TextView
    private lateinit var creditScoreText: TextView
    private lateinit var totalSavingsText: TextView
    private lateinit var activeLoansText: TextView
    private lateinit var tvAITip: TextView
    private lateinit var profileImage: ShapeableImageView
    
    private lateinit var savingsCard: MaterialCardView
    private lateinit var loansCard: MaterialCardView
    private lateinit var chamaCard: MaterialCardView
    private lateinit var knowledgeCard: MaterialCardView
    private lateinit var supportCard: MaterialCardView

    private val groqApiKey = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        userNameText = findViewById(R.id.userNameText)
        creditScoreText = findViewById(R.id.creditScore)
        totalSavingsText = findViewById(R.id.totalSavingsText)
        activeLoansText = findViewById(R.id.activeLoansText)
        tvAITip = findViewById(R.id.tvAITip)
        profileImage = findViewById(R.id.profileImage)
        
        savingsCard = findViewById(R.id.savingsCard)
        loansCard = findViewById(R.id.loansCard)
        chamaCard = findViewById(R.id.chamaCard)
        knowledgeCard = findViewById(R.id.knowledgeCard)
        supportCard = findViewById(R.id.supportCard)

        loadUserData()
        loadSavingsData()
        fetchDailyTip()

        profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        savingsCard.setOnClickListener {
            startActivity(Intent(this, SavingsActivity::class.java))
        }

        loansCard.setOnClickListener {
            startActivity(Intent(this, LoansActivity::class.java))
        }

        chamaCard.setOnClickListener {
            startActivity(Intent(this, ChamaActivity::class.java))
        }

        knowledgeCard.setOnClickListener {
            startActivity(Intent(this, KnowledgeActivity::class.java))
        }

        supportCard.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }
    }

    private fun fetchDailyTip() {
        lifecycleScope.launch {
            try {
                val request = GroqRequest(
                    model = "llama-3.1-8b-instant",
                    messages = listOf(
                        GroqMessage(
                            role = "system",
                            content = "You are a financial expert. Give one short, powerful financial tip (max 15 words) for a Kenyan woman entrepreneur."
                        ),
                        GroqMessage(
                            role = "user",
                            content = "Give me today's tip."
                        )
                    )
                )

                val response = GroqClient.instance.getChatCompletion("Bearer $groqApiKey", request)
                if (response.choices.isNotEmpty()) {
                    tvAITip.text = response.choices[0].message.content.replace("\"", "")
                }
            } catch (e: Exception) {
                tvAITip.text = "Save at least 10% of your daily income in a Chama."
                Log.e("DashboardAI", "Error fetching tip: ${e.message}")
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "User"
                    val score = snapshot.child("creditScore").getValue(Int::class.java) ?: 750
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    
                    userNameText.text = "Hello, $name"
                    creditScoreText.text = score.toString()

                    if (!profileImageUrl.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            val bitmap = withContext(Dispatchers.IO) {
                                decodeImage(profileImageUrl)
                            }
                            if (bitmap != null) {
                                profileImage.setImageBitmap(bitmap)
                                profileImage.setPadding(0, 0, 0, 0)
                                profileImage.imageTintList = null
                                profileImage.clearColorFilter()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    userNameText.text = "Welcome Back"
                }
            })
    }

    private fun loadSavingsData() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Savings").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalSavings = 0.0
                    for (goalSnapshot in snapshot.children) {
                        val savedAmount = goalSnapshot.child("savedAmount").getValue(Double::class.java) ?: 0.0
                        totalSavings += savedAmount
                    }
                    
                    totalSavingsText.text = CurrencyUtils.formatKes(totalSavings)
                    activeLoansText.text = CurrencyUtils.formatKes(0.0)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun decodeImage(base64Str: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
