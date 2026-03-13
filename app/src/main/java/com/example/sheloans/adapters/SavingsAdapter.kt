package com.example.sheloans.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sheloans.R
import com.example.sheloans.models.SavingsGoal
import com.example.sheloans.utils.formatCurrency

class SavingsAdapter(
    private val goals: List<SavingsGoal>,
    private val onDepositClick: (SavingsGoal) -> Unit,
    private val onWithdrawClick: (SavingsGoal) -> Unit
) : RecyclerView.Adapter<SavingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGoalName)
        val tvStatus: TextView = view.findViewById(R.id.tvGoalStatus)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val btnDeposit: Button = view.findViewById(R.id.btnAddFunds)
        val btnWithdraw: Button = view.findViewById(R.id.btnWithdraw)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_savings_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvName.text = goal.name
        holder.tvStatus.text = "${formatCurrency(goal.savedAmount)} of ${formatCurrency(goal.targetAmount)}"
        
        val progress = if (goal.targetAmount > 0) {
            ((goal.savedAmount / goal.targetAmount) * 100).toInt()
        } else 0
        holder.progressBar.progress = progress

        holder.btnDeposit.setOnClickListener {
            onDepositClick(goal)
        }
        
        holder.btnWithdraw.setOnClickListener {
            onWithdrawClick(goal)
        }
    }

    override fun getItemCount() = goals.size
}
