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

        getResult()

        binding.toHome.setOnClickListener {
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getResult(){
        val totalEmissionString = intent.getStringExtra("TOTAL_EMISSION")
        val totalDistanceString = intent.getStringExtra("TOTAL_DISTANCE")

        val totalEmission = totalEmissionString?.toDoubleOrNull() ?: 0.0
        val totalDistance = totalDistanceString?.toDoubleOrNull() ?: 0.0

        binding.totalEmission.text = "$totalEmission kg CO2"
        binding.totalDistance.text = "$totalDistance km"
    }
}