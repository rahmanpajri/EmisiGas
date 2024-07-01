package com.jri.emisigas.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jri.emisigas.R
import com.jri.emisigas.result.Result
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(private val listHistory: ArrayList<Result>): RecyclerView.Adapter<HistoryAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listHistory.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentItem = listHistory[position]
        val dateString = currentItem.date
        val inputFormat = SimpleDateFormat("EEEE, dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)?: ""

        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        holder.day.text = dayFormat.format(date)
        holder.date.text = dateFormat.format(date)
        holder.consumptionCO2.text = currentItem.result + " kg CO2"
        holder.distance.text = currentItem.distance + " km"
        val ch4Result = currentItem.result_CH4.ifEmpty { "0.0" }
        holder.consumptionCH4.text = "$ch4Result kg CH4"
        val n2oResult = currentItem.result_N2O.ifEmpty { "0.0" }
        holder.consumption_n2o.text = "$n2oResult kg N2O"
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Your CO2 Consumption is ${currentItem.result}", Toast.LENGTH_SHORT).show()
        }
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val day: TextView = itemView.findViewById(R.id.day)
        val date: TextView = itemView.findViewById(R.id.date)
        val consumptionCO2: TextView = itemView.findViewById(R.id.consumption)
        val distance: TextView = itemView.findViewById(R.id.distance)
        val consumptionCH4: TextView = itemView.findViewById(R.id.consumption_ch4)
        val consumption_n2o: TextView = itemView.findViewById(R.id.consumption_n2o)
    }
}
