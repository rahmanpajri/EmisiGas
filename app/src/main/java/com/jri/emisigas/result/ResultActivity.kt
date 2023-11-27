package com.jri.emisigas.result

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jri.emisigas.MainActivity
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

        binding.toHome.setOnClickListener {
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}