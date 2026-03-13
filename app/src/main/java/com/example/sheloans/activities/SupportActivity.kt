package com.example.sheloans.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sheloans.R
import com.example.sheloans.models.User
import com.example.sheloans.models.ViolationReport
import com.example.sheloans.utils.NotificationHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SupportActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var currentUserProfile: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val spinnerIssueType = findViewById<Spinner>(R.id.spinnerIssueType)
        val etOtherIssue = findViewById<TextInputEditText>(R.id.etOtherIssue)
        val btnReport = findViewById<Button>(R.id.btnReport)

        requestNotificationPermission()
        fetchUserDetails()

        val issueTypes = listOf(
            "Unexpected Interest Increase",
            "Hidden Service Fees",
            "Harassment by Debt Collector",
            "Privacy/Data Violation",
            "Breach of Agreement",
            "Other (Describe below)"
        )

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, issueTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerIssueType.adapter = spinnerAdapter

        btnReport.setOnClickListener {
            val selectedIssue = spinnerIssueType.selectedItem.toString()
            val description = etOtherIssue.text.toString().trim()

            if (selectedIssue == "Other (Describe below)" && description.isEmpty()) {
                Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show()
            } else {
                submitReport(selectedIssue, description, etOtherIssue)
            }
        }

        findViewById<Button>(R.id.btnCBK).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.centralbank.go.ke/"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCOFEK).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cofek.africa/"))
            startActivity(intent)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun fetchUserDetails() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUserProfile = snapshot.getValue(User::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun submitReport(issue: String, desc: String, inputField: TextInputEditText) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val reportRef = database.reference.child("Reports").push()
        val reportId = reportRef.key ?: return

        val displayIssue = if (issue.contains("Other")) "your concern" else issue

        val report = ViolationReport(
            reportId = reportId,
            userId = userId,
            userName = currentUserProfile?.name ?: "Unknown User",
            userPhone = currentUserProfile?.phone ?: "No Phone",
            issueType = issue,
            description = desc,
            timestamp = System.currentTimeMillis(),
            status = "Pending"
        )

        reportRef.setValue(report).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Report received. Check your notifications.", Toast.LENGTH_SHORT).show()
                
                NotificationHelper.showNotification(
                    this,
                    "Complaint Received",
                    "We have received your complaint regarding $displayIssue. Our legal team will contact you shortly for a follow up."
                )
                
                inputField.text?.clear()
            } else {
                Toast.makeText(this, "Error submitting report.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
