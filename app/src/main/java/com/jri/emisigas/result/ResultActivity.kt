package com.jri.emisigas.result

import android.annotation.SuppressLint
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

    @SuppressLint("SetTextI18n")
    private fun getResult(){
        val totalEmissionCO2String = intent.getStringExtra("TOTAL_EMISSION_CO2")
        val totalEmissionCH4String = intent.getDoubleExtra("TOTAL_EMISSION_CH4", 0.0)
        val totalEmissionN2OString = intent.getDoubleExtra("TOTAL_EMISSION_N2O", 0.0)
        val totalDistanceString = intent.getStringExtra("TOTAL_DISTANCE")

        val totalEmissionCO2 = totalEmissionCO2String?.toDoubleOrNull() ?: 0.0
        val totalEmissionCH4 = if (totalEmissionCH4String == 0.0) "0.0" else String.format("%.8f", totalEmissionCH4String)
        val totalEmissionN2O = if (totalEmissionN2OString == 0.0) "0.0" else String.format("%.8f", totalEmissionN2OString)
        val totalDistance = totalDistanceString?.toDoubleOrNull() ?: 0.0

        binding.totalEmissionCo2.text = "$totalEmissionCO2 kg CO2"
        binding.totalEmissionCh4.text = "$totalEmissionCH4 kg CH4"
        binding.totalEmissionN2o.text = "$totalEmissionN2O kg N2O"
        binding.totalDistance.text = "$totalDistance km"
    }
}