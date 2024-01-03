package com.jri.emisigas.tips

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityDetailTipsBinding

class DetailTipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTipsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detail Tips"
            setHomeAsUpIndicator(R.drawable.twotone_arrow_back_ios_24)
        }

        val tips: Tips? = intent.getParcelableExtra("key_tips")

        if(tips != null){
            Glide.with(this).load(tips.image).into(binding.photoReceived)
            binding.tipsName.text = tips.title
            binding.description.text = tips.description
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}