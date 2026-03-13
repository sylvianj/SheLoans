package com.example.sheloans.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.sheloans.R
import com.example.sheloans.models.LoanProvider

class LoanAdapter(private val items: List<LoanProvider>) : BaseAdapter() {
    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_loan_provider, parent, false)

        val item = items[position]
        
        view.findViewById<TextView>(R.id.tvBankName).text = item.name
        view.findViewById<TextView>(R.id.tvLoanDetails).text = "${item.maxLoan} • ${item.interest} Interest"
        view.findViewById<TextView>(R.id.tvDescription).text = item.description
        
        view.findViewById<Button>(R.id.btnApply).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.website))
            parent?.context?.startActivity(intent)
        }

        return view
    }
}
