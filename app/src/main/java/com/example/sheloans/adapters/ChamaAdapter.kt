package com.example.sheloans.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sheloans.R
import com.example.sheloans.models.Chama

class ChamaAdapter(
    private val chamaList: List<Chama>,
    private val onItemClick: (Chama) -> Unit,
    private val onDeleteClick: (Chama) -> Unit
) : RecyclerView.Adapter<ChamaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvChamaName)
        val tvBalance: TextView = view.findViewById(R.id.tvChamaBalance)
        val tvViewDetails: TextView = view.findViewById(R.id.tvViewDetails)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteChama)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chama, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chama = chamaList[position]
        holder.tvName.text = chama.name
        holder.tvBalance.text = "Group Balance: KES ${chama.balance}"
        
        holder.itemView.setOnClickListener {
            onItemClick(chama)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(chama)
        }
    }

    override fun getItemCount() = chamaList.size
}
