package com.jri.emisigas.jenis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.databinding.ActivityJenisBbBinding
import com.jri.emisigas.maps.MapsActivity
import com.jri.emisigas.tips.ListTipsAdapter
import com.jri.emisigas.tips.Tips

class JenisBbActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJenisBbBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var rvJenis: RecyclerView
    private val list = ArrayList<JenisBB>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJenisBbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvJenis = binding.rvJenis
        rvJenis.layoutManager = LinearLayoutManager(this)
        rvJenis.setHasFixedSize(true)

        getJenisData()
    }

    private fun getJenisData() {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        dbRef = database.reference.child("jenis_bb")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (jenisSnapshot in snapshot.children){
                        val jenis = jenisSnapshot.getValue(JenisBB::class.java)

                        list.add(jenis!!)
                    }
                    rvJenis.adapter = ListJenisBBAdapter(list)
                }else{
                    Toast.makeText(this@JenisBbActivity, "Data is Null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@JenisBbActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}