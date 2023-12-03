package com.jri.emisigas.tips

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jri.emisigas.databinding.ItemRowTipsBinding

class ListTipsAdapter(private val listTips: ArrayList<Tips>): RecyclerView.Adapter<ListTipsAdapter.ListViewHolder>() {

    class ListViewHolder(var binding: ItemRowTipsBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemRowTipsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (title, id, description) = listTips[position]
        holder.binding.tipsName.text = title
        holder.binding.tipsDescription.text = description
        holder.itemView.setOnClickListener{
            val intentDetail = Intent(holder.itemView.context, DetailTipsActivity::class.java)
            intentDetail.putExtra("key_tips", listTips[holder.adapterPosition])
            holder.itemView.context.startActivity(intentDetail)
        }

    }

    override fun getItemCount(): Int = listTips.size

}