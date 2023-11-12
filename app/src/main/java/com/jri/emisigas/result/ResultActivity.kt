package com.jri.emisigas.result

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalEmission = intent.getDoubleExtra("TOTAL_EMISSION", 0.0)
        val totalDistance = intent.getDoubleExtra("TOTAL_DISTANCE", 0.0)

        binding.totalEmission.text = "Total Emission: $totalEmission kg CO2"
        binding.totalDistance.text = "Total Distance: $totalDistance km"
    }
}