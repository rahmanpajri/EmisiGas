package com.jri.emisigas.jenis

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jri.emisigas.databinding.ItemButtonBinding
import com.jri.emisigas.tahun.TahunPembuatanActivity

class ListJenisBBAdapter(private val listJenis: ArrayList<JenisBB>): RecyclerView.Adapter<ListJenisBBAdapter.ListViewHolder>() {

    class ListViewHolder(var binding: ItemButtonBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (id, name) = listJenis[position]
        holder.binding.nameButton.text = name
        holder.itemView.setOnClickListener{
            val selectedJenisBB = listJenis[position]
            val intent = Intent(holder.itemView.context, TahunPembuatanActivity::class.java)
            intent.putExtra("jenis_id", selectedJenisBB.id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = listJenis.size

}