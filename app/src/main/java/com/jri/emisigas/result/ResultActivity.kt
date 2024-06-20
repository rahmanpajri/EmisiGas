package com.jri.emisigas.result

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jri.emisigas.MainActivity
import com.jri.emisigas.databinding.ActivityResultBinding
import com.jri.emisigas.maps.MapsActivity

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
        val totalEmissionCO2String = intent.getStringExtra("TOTAL_EMISSION_CO2")
        val totalEmissionCH4String = intent.getDoubleExtra("TOTAL_EMISSION_CH4", 0.0)
        val totalEmissionN2OString = intent.getDoubleExtra("TOTAL_EMISSION_N2O", 0.0)
        val totalDistanceString = intent.getStringExtra("TOTAL_DISTANCE")
        Log.d("ResultActivity", "Total Emission Result: $totalEmissionCH4String kg CH4")

        val totalEmissionCO2 = totalEmissionCO2String?.toDoubleOrNull() ?: 0.0
        val totalEmissionCH4 = String.format("%.8f kg CH4", totalEmissionCH4String)
        val totalEmissionN2O = String.format("%.8f kg N2O", totalEmissionN2OString)
        val totalDistance = totalDistanceString?.toDoubleOrNull() ?: 0.0

        binding.totalEmissionCo2.text = "$totalEmissionCO2 kg CO2"
        binding.totalEmissionCh4.text = "$totalEmissionCH4"
        binding.totalEmissionN2o.text = "$totalEmissionN2O"
        binding.totalDistance.text = "$totalDistance km"
    }
}