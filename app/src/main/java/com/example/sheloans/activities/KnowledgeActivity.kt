package com.example.sheloans.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sheloans.R
import com.example.sheloans.models.GroqMessage
import com.example.sheloans.models.GroqRequest
import com.example.sheloans.utils.GroqClient
import kotlinx.coroutines.launch

class KnowledgeActivity : AppCompatActivity() {

    private lateinit var etQuestion: EditText
    private lateinit var btnAsk: ImageButton
    private lateinit var aiResponseText: TextView
    private lateinit var aiLoading: ProgressBar
    private lateinit var chatScrollView: ScrollView

    private val groqApiKey = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge)

        etQuestion = findViewById(R.id.etQuestion)
        btnAsk = findViewById(R.id.btnAsk)
        aiResponseText = findViewById(R.id.aiResponseText)
        aiLoading = findViewById(R.id.aiLoading)
        chatScrollView = findViewById(R.id.chatScrollView)

        btnAsk.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                askAI(question)
            } else {
                Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askAI(question: String) {
        aiLoading.visibility = View.VISIBLE
        aiResponseText.text = "Consulting SheLoans AI (Groq)..."
        btnAsk.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val request = GroqRequest(
                    model = "llama-3.1-8b-instant",
                    messages = listOf(
                        GroqMessage(
                            role = "system",
                            content = "You are a professional financial advisor for women in Kenya. Be concise."
                        ),
                        GroqMessage(
                            role = "user",
                            content = question
                        )
                    )
                )

                val response = GroqClient.instance.getChatCompletion("Bearer $groqApiKey", request)
                
                if (response.choices.isNotEmpty()) {
                    val aiText = response.choices[0].message.content
                    aiResponseText.text = aiText
                    etQuestion.text.clear()
                    chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
                } else {
                    aiResponseText.text = "AI returned an empty response. Try again."
                }
            } catch (e: Exception) {
                Log.e("GroqError", "FULL ERROR: ${e.message}", e)
                
                val errorMsg = e.message ?: "Unknown Error"
                
                when {
                    errorMsg.contains("400") -> {
                        aiResponseText.text = "TECHNICAL ERROR (400):\nCheck if model name 'llama-3.1-8b-instant' is correct for your region."
                    }
                    errorMsg.contains("401") -> {
                        aiResponseText.text = "API KEY ERROR:\nInvalid API key. Re-copy from console.groq.com."
                    }
                    else -> {
                        aiResponseText.text = "TECHNICAL ERROR:\n$errorMsg"
                    }
                }
            } finally {
                aiLoading.visibility = View.GONE
                btnAsk.isEnabled = true
            }
        }
    }
}
