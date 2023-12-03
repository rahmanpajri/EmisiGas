package com.jri.emisigas.tahun

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jri.emisigas.databinding.ItemButtonBinding
import com.jri.emisigas.jenis.JenisBB
import com.jri.emisigas.jenis.ListJenisBBAdapter
import com.jri.emisigas.maps.MapsActivity

class ListTahunPembuatanAdapter(private val listTahun: ArrayList<TahunPembuatan>, private val selectedJenisBB: String?): RecyclerView.Adapter<ListTahunPembuatanAdapter.ListViewHolder>() {

    class ListViewHolder(var binding: ItemButtonBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (id, name) = listTahun[position]
        val selectedTahunPembuatan = listTahun[position]
        holder.binding.nameButton.text = name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, MapsActivity::class.java)
            intent.putExtra("jenis_id", selectedJenisBB)
            intent.putExtra("tahun_id", selectedTahunPembuatan.id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = listTahun.size

}