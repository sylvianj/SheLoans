package com.example.sheloans.activities

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sheloans.R
import com.example.sheloans.adapters.ChamaAdapter
import com.example.sheloans.models.Chama
import com.example.sheloans.models.SavingsGoal
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChamaActivity : AppCompatActivity() {

    private lateinit var createChama: Button
    private lateinit var chamaRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val chamas = mutableListOf<Chama>()
    private lateinit var adapter: ChamaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chama)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        createChama = findViewById(R.id.createChama)
        chamaRecyclerView = findViewById(R.id.chamaRecyclerView)
        
        adapter = ChamaAdapter(
            chamas,
            onItemClick = { chama -> showChamaDetails(chama) },
            onDeleteClick = { chama -> confirmDeleteChama(chama) }
        )
        chamaRecyclerView.layoutManager = LinearLayoutManager(this)
        chamaRecyclerView.adapter = adapter

        loadUserChamas()

        createChama.setOnClickListener {
            showCreateChamaDialog()
        }
    }

    private fun loadUserChamas() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Chamas").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chamas.clear()
                    for (chamaSnapshot in snapshot.children) {
                        val chama = chamaSnapshot.getValue(Chama::class.java)
                        if (chama != null) {
                            chamas.add(chama)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChamaActivity, "Failed to load Chamas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmDeleteChama(chama: Chama) {
        AlertDialog.Builder(this)
            .setTitle("Delete Chama")
            .setMessage("Are you sure you want to delete '${chama.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteChama(chama)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteChama(chama: Chama) {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Chamas").child(userId).child(chama.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Chama deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete Chama", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChamaDetails(chama: Chama) {
        val userId = auth.currentUser?.uid ?: return
        
        database.reference.child("Savings").child(userId)
            .orderByChild("chamaId").equalTo(chama.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savings = mutableListOf<String>()
                    var totalUserSaved = 0.0
                    
                    for (goalSnapshot in snapshot.children) {
                        val goal = goalSnapshot.getValue(SavingsGoal::class.java)
                        if (goal != null) {
                            savings.add("${goal.name}: KES ${goal.savedAmount}")
                            totalUserSaved += goal.savedAmount
                        }
                    }

                    val message = if (savings.isEmpty()) {
                        "You haven't saved anything in this Chama yet."
                    } else {
                        "Your contributions:\n\n" + savings.joinToString("\n") + 
                        "\n\nTotal Personal Savings: KES $totalUserSaved"
                    }

                    AlertDialog.Builder(this@ChamaActivity)
                        .setTitle("${chama.name} Details")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChamaActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showCreateChamaDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Chama")

        val inputLayout = TextInputLayout(this)
        inputLayout.hint = "Chama Name"
        val padding = (24 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(padding, padding / 2, padding, padding / 2)
        
        val input = TextInputEditText(this)
        inputLayout.addView(input)
        container.addView(inputLayout)
        container.layoutParams = params
        builder.setView(container)

        builder.setPositiveButton("Create") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                saveNewChama(name)
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveNewChama(name: String) {
        val userId = auth.currentUser?.uid ?: return
        val chamaId = database.reference.child("Chamas").child(userId).push().key ?: return
        
        val chamaData = mapOf(
            "id" to chamaId,
            "name" to name,
            "balance" to 0L
        )

        database.reference.child("Chamas").child(userId).child(chamaId).setValue(chamaData)
            .addOnSuccessListener {
                Toast.makeText(this, "Chama '$name' created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create Chama", Toast.LENGTH_SHORT).show()
            }
    }
}
