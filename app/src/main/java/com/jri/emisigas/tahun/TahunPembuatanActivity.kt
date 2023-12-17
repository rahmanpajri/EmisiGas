package com.jri.emisigas.tahun

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
import com.jri.emisigas.databinding.ActivityTahunPembuatanBinding

class TahunPembuatanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTahunPembuatanBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var rvTahun: RecyclerView
    private val list = ArrayList<TahunPembuatan>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTahunPembuatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvTahun = binding.rvTahun
        rvTahun.layoutManager = LinearLayoutManager(this)
        rvTahun.setHasFixedSize(true)

        getTahunData()
    }

    private fun getTahunData() {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        dbRef = database.reference.child("tahun_pembuatan")

        val jenisId = intent.getStringExtra("jenis_id")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (tahunSnapshot in snapshot.children){
                        val tahun = tahunSnapshot.getValue(TahunPembuatan::class.java)

                        list.add(tahun!!)
                    }
                    rvTahun.adapter = ListTahunPembuatanAdapter(list, jenisId)
                }else{
                    Toast.makeText(this@TahunPembuatanActivity, "Data is Null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TahunPembuatanActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}