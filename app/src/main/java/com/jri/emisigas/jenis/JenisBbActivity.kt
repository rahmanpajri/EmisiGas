package com.jri.emisigas.jenis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jri.emisigas.databinding.ActivityJenisBbBinding
import com.jri.emisigas.maps.MapsActivity

class JenisBbActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJenisBbBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJenisBbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button2.setOnClickListener {
            val intent = Intent(this@JenisBbActivity, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}