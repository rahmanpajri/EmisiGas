package com.jri.emisigas.prediction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jri.emisigas.databinding.ItemPredictionBinding

class PredictionAdapter(private val predictions: List<PredictionItem>) : RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val binding = ItemPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PredictionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.bind(prediction)
    }

    override fun getItemCount(): Int = predictions.size

    class PredictionViewHolder(private val binding: ItemPredictionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(prediction: PredictionItem) {
            binding.dateTextView.text = prediction.date
            binding.predictionTextView.text = "Prediction: ${prediction.prediction}"
        }
    }
}