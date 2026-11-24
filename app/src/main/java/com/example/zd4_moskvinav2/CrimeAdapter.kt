package com.example.zd4_moskvinav2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CrimeAdapter(
    private val onItemClick: (Crime) -> Unit
) : RecyclerView.Adapter<CrimeAdapter.CrimeViewHolder>() {

    private var crimes: List<Crime> = listOf()

    fun submitList(newCrimes: List<Crime>) {
        crimes = newCrimes
        notifyDataSetChanged()
    }

    class CrimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_crime_title)
        val tvDate: TextView = view.findViewById(R.id.tv_crime_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crime, parent, false)
        return CrimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CrimeViewHolder, position: Int) {
        val crime = crimes[position]
        holder.tvTitle.text = crime.title
        holder.tvDate.text = crime.date
        holder.itemView.setOnClickListener { onItemClick(crime) }
    }

    override fun getItemCount() = crimes.size
}