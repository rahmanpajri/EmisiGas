package com.jri.emisigas.onBoarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jri.emisigas.databinding.SliderItemBinding

class SliderAdapter(private val slider: List<Slider>) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(binding: SliderItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private val textTitle = binding.sliderTitle
        private val textDescription = binding.sliderDescription
        private val image = binding.imageBanner

        fun bind(slider: Slider) {
            textTitle.text = slider.title
            textDescription.text = slider.descriptor
            Glide.with(itemView)
                .load(slider.image)
                .into(image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = SliderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return slider.size
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(slider[position])
    }
}
