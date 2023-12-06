package com.jri.emisigas.vehicle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jri.emisigas.MainActivity
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityChangeProfileBinding
import com.jri.emisigas.databinding.ActivityChangeVehicleBinding
import com.jri.emisigas.result.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChangeVehicleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeVehicleBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.changeButton.setOnClickListener {
            changeData()
        }
    }

    private fun changeData() {
        val uid = auth.currentUser!!.uid
        val brand = binding.brandEditText.text.toString()
        val plate = binding.plateEditText.text.toString()
        val capacity = binding.machineEditText.text.toString()

        val result = Vehicle(brand, capacity, plate, uid)

        val database = FirebaseDatabase.getInstance().reference.child("vehicle").push()

        database.setValue(result)
            .addOnSuccessListener {
                Toast.makeText(this, "Vehicle added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add Vehicle", Toast.LENGTH_SHORT).show()
            }
    }
}