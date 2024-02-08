package com.jri.emisigas.tips

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityTipsBinding

@Suppress("DEPRECATION")
class TipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipsBinding
    private lateinit var dbref: DatabaseReference
    private lateinit var rvTips: RecyclerView
    private lateinit var progressDialog: ProgressDialog
    private val list = ArrayList<Tips>()
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Tips"
            setHomeAsUpIndicator(R.drawable.twotone_arrow_back_ios_24)
        }

        progressDialog = ProgressDialog(this, com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        rvTips = binding.rvTips
        rvTips.layoutManager = LinearLayoutManager(this)
        rvTips.setHasFixedSize(true)
        getTips()
    }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun getTips() {
        showLoading()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        dbref = database.reference.child("tips")
        dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                hideLoading()
                if(snapshot.exists()){
                    for (tipsSnapshot in snapshot.children){
                        val tips = tipsSnapshot.getValue(Tips::class.java)
                        list.add(tips!!)
                    }
                    rvTips.adapter = ListTipsAdapter(list)
                }else{
                    Toast.makeText(this@TipsActivity, "Data is Null", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                hideLoading()
                Toast.makeText(this@TipsActivity, "Database Error, Check your Connection", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}