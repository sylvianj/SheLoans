package com.example.sheloans.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sheloans.R
import com.example.sheloans.adapters.SavingsAdapter
import com.example.sheloans.models.SavingsGoal
import com.example.sheloans.utils.*
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavingsActivity : AppCompatActivity() {

    private lateinit var goalName: TextInputEditText
    private lateinit var goalAmount: TextInputEditText
    private lateinit var createGoal: Button
    private lateinit var cbIsChama: MaterialCheckBox
    private lateinit var chamaSpinner: Spinner
    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val goalsList = mutableListOf<SavingsGoal>()
    private val chamaNames = mutableListOf<String>()
    private val chamaIds = mutableListOf<String>()
    private lateinit var adapter: SavingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        goalName = findViewById(R.id.goalName)
        goalAmount = findViewById(R.id.goalAmount)
        createGoal = findViewById(R.id.createGoal)
        cbIsChama = findViewById(R.id.cbIsChama)
        chamaSpinner = findViewById(R.id.chamaSpinner)
        goalsRecyclerView = findViewById(R.id.goalsRecyclerView)

        adapter = SavingsAdapter(
            goalsList,
            onDepositClick = { goal -> handleDeposit(goal) },
            onWithdrawClick = { goal -> handleWithdraw(goal) }
        )
        goalsRecyclerView.layoutManager = LinearLayoutManager(this)
        goalsRecyclerView.adapter = adapter

        cbIsChama.setOnCheckedChangeListener { _, isChecked ->
            chamaSpinner.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) loadUserChamasForSpinner()
        }

        loadSavingsGoals()

        createGoal.setOnClickListener {
            val name = goalName.text.toString().trim()
            val amountStr = goalAmount.text.toString().trim()
            val amount = amountStr.toDoubleOrNull()

            if (name.isNotEmpty() && amount != null) {
                var selectedChamaId: String? = null
                var selectedChamaName: String? = null
                
                if (cbIsChama.isChecked && chamaIds.isNotEmpty()) {
                    val pos = chamaSpinner.selectedItemPosition
                    selectedChamaId = chamaIds[pos]
                    selectedChamaName = chamaNames[pos]
                }
                
                saveGoal(name, amount, selectedChamaId, selectedChamaName)
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserChamasForSpinner() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Chamas").child(userId).get().addOnSuccessListener { snapshot ->
            chamaNames.clear()
            chamaIds.clear()
            for (chamaSnapshot in snapshot.children) {
                val id = chamaSnapshot.child("id").getValue(String::class.java)
                val name = chamaSnapshot.child("name").getValue(String::class.java)
                if (id != null && name != null) {
                    chamaIds.add(id)
                    chamaNames.add(name)
                }
            }
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chamaNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            chamaSpinner.adapter = spinnerAdapter
        }
    }

    private fun handleDeposit(goal: SavingsGoal) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Deposit via M-Pesa")
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = (24 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, margin / 2, margin, margin / 2)
        val input = TextInputEditText(this)
        input.hint = "Amount (KES)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Send") { _, _ ->
            val amount = input.text.toString().toIntOrNull()
            if (amount != null && amount > 0) fetchUserPhoneAndPay(goal, amount)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun handleWithdraw(goal: SavingsGoal) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Withdraw to M-Pesa")
        builder.setMessage("Available balance: ${formatCurrency(goal.savedAmount)}")
        
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = (24 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, margin / 2, margin, margin / 2)
        val input = TextInputEditText(this)
        input.hint = "Amount to withdraw (KES)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Withdraw") { _, _ ->
            val amount = input.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                if (amount <= goal.savedAmount) {
                    performWithdrawal(goal, amount)
                } else {
                    Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun performWithdrawal(goal: SavingsGoal, amount: Double) {
        val userId = auth.currentUser?.uid ?: return
        val newTotal = goal.savedAmount - amount
        
        database.reference.child("Savings").child(userId).child(goal.id).child("savedAmount").setValue(newTotal)
            .addOnSuccessListener {
                if (goal.chamaId != null) {
                    database.reference.child("Chamas").child(userId).child(goal.chamaId).child("balance")
                        .runTransaction(object : Transaction.Handler {
                            override fun doTransaction(m: MutableData): Transaction.Result {
                                val current = m.getValue(Long::class.java) ?: 0L
                                m.value = current - amount.toLong()
                                return Transaction.success(m)
                            }
                            override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
                        })
                }
                Toast.makeText(this, "Withdrawal successful! Processing to M-Pesa...", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Withdrawal failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserPhoneAndPay(goal: SavingsGoal, amount: Int) {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Users").child(userId).child("phone").get().addOnSuccessListener { snapshot ->
            var phone = snapshot.getValue(String::class.java) ?: ""
            if (phone.startsWith("0")) phone = "254" + phone.substring(1)
            getAccessTokenAndPush(goal, amount, phone)
        }
    }

    private fun getAccessTokenAndPush(goal: SavingsGoal, amount: Int, phone: String) {
        val authHeader = MpesaClient.getAuthHeader()
        MpesaClient.instance.getAccessToken(authHeader).enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    performStkPush(response.body()?.accessToken ?: "", goal, amount, phone)
                }
            }
            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                Toast.makeText(this@SavingsActivity, "Failed to connect to M-Pesa", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performStkPush(token: String, goal: SavingsGoal, amount: Int, phone: String) {
        val timestamp = MpesaClient.getTimestamp()
        val password = MpesaClient.getPassword(MpesaClient.BUSINESS_SHORT_CODE, MpesaClient.PASSKEY, timestamp)
        val request = STKPushRequest(
            businessShortCode = MpesaClient.BUSINESS_SHORT_CODE,
            password = password, timestamp = timestamp, transactionType = "CustomerPayBillOnline",
            amount = amount, partyA = phone, partyB = MpesaClient.BUSINESS_SHORT_CODE,
            phoneNumber = phone, callbackUrl = "https://mydomain.com/path",
            accountReference = goal.name.take(12), transactionDesc = "Savings Deposit"
        )
        MpesaClient.instance.sendSTKPush("Bearer $token", request).enqueue(object : Callback<STKPushResponse> {
            override fun onResponse(call: Call<STKPushResponse>, response: Response<STKPushResponse>) {
                if (response.isSuccessful && response.body()?.responseCode == "0") {
                    val checkoutRequestId = response.body()?.checkoutRequestId
                    if (checkoutRequestId != null) {
                        Toast.makeText(this@SavingsActivity, "STK Push sent. Please enter PIN on phone.", Toast.LENGTH_LONG).show()
                        listenForPaymentVerification(checkoutRequestId, goal, amount.toDouble())
                    }
                } else {
                    Toast.makeText(this@SavingsActivity, "STK Push failed: ${response.body()?.customerMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<STKPushResponse>, t: Throwable) {
                Toast.makeText(this@SavingsActivity, "M-Pesa request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listenForPaymentVerification(checkoutRequestId: String, goal: SavingsGoal, amount: Double) {
        val userId = auth.currentUser?.uid ?: return
        val paymentRef = database.reference.child("Payments").child(checkoutRequestId)
        
        paymentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("ResultCode").getValue(Int::class.java)
                if (status == 0) {
                    updateSavingsAndScore(goal, amount)
                    paymentRef.removeEventListener(this)
                    Toast.makeText(this@SavingsActivity, "Payment Successful!", Toast.LENGTH_SHORT).show()
                } else if (status != null) {
                    Toast.makeText(this@SavingsActivity, "Payment failed or cancelled.", Toast.LENGTH_SHORT).show()
                    paymentRef.removeEventListener(this)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateSavingsAndScore(goal: SavingsGoal, amount: Double) {
        val userId = auth.currentUser?.uid ?: return
        val newTotal = goal.savedAmount + amount
        database.reference.child("Savings").child(userId).child(goal.id).child("savedAmount").setValue(newTotal)
            .addOnSuccessListener {
                if (goal.chamaId != null) {
                    database.reference.child("Chamas").child(userId).child(goal.chamaId).child("balance")
                        .runTransaction(object : Transaction.Handler {
                            override fun doTransaction(m: MutableData): Transaction.Result {
                                val current = m.getValue(Long::class.java) ?: 0L
                                m.value = current + amount.toLong()
                                return Transaction.success(m)
                            }
                            override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
                        })
                }
                incrementCreditScore(userId)
            }
    }

    private fun incrementCreditScore(userId: String) {
        database.reference.child("Users").child(userId).child("creditScore").runTransaction(object : Transaction.Handler {
            override fun doTransaction(m: MutableData): Transaction.Result {
                val s = m.getValue(Int::class.java) ?: 750
                if (s < 1000) m.value = s + 10
                return Transaction.success(m)
            }
            override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {
                if (c) Toast.makeText(this@SavingsActivity, "Credit Score Improved!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSavingsGoals() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Savings").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                goalsList.clear()
                for (gs in s.children) {
                    val g = gs.getValue(SavingsGoal::class.java)
                    if (g != null) goalsList.add(g)
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun saveGoal(name: String, amount: Double, chamaId: String?, chamaName: String?) {
        val userId = auth.currentUser?.uid ?: return
        val goalId = database.reference.child("Savings").child(userId).push().key ?: return
        val goalData = SavingsGoal(id = goalId, name = name, targetAmount = amount, savedAmount = 0.0, chamaId = chamaId, chamaName = chamaName)
        database.reference.child("Savings").child(userId).child(goalId).setValue(goalData)
            .addOnSuccessListener {
                goalName.text?.clear()
                goalAmount.text?.clear()
                cbIsChama.isChecked = false
                Toast.makeText(this, "Goal Created!", Toast.LENGTH_SHORT).show()
            }
    }
}
