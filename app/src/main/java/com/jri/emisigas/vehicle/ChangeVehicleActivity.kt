package com.jri.emisigas.vehicle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.MainActivity
import com.jri.emisigas.databinding.ActivityChangeVehicleBinding

class ChangeVehicleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeVehicleBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        displayVehicleData()

        binding.changeButton.setOnClickListener {
            changeData()
        }
    }

    private fun displayVehicleData() {
        val uid = auth.currentUser!!.uid
        val database = FirebaseDatabase.getInstance().reference.child("vehicle")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val vehicle = data.getValue(Vehicle::class.java)
                        if (vehicle != null && vehicle.user_id == uid) {
                            val brand = vehicle.brand
                            val plate = vehicle.plate
                            val capacity = vehicle.capacity

                            // Set retrieved data to the respective EditText fields
                            binding.brandEditText.setText(brand)
                            binding.plateEditText.setText(plate)
                            binding.machineEditText.setText(capacity)
                            break // Break the loop after finding the user's data
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }



    private fun changeData() {
        val uid = auth.currentUser!!.uid
        val brand = binding.brandEditText.text.toString()
        val plate = binding.plateEditText.text.toString()
        val capacity = binding.machineEditText.text.toString()

        val result = Vehicle(brand, capacity, plate, uid)

        val database = FirebaseDatabase.getInstance().reference.child("vehicle")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userVehicleExists = false
                snapshot.children.forEach { vehicleData ->
                    val vehicle = vehicleData.getValue(Vehicle::class.java)
                    if (vehicle != null && vehicle.user_id == uid) {
                        userVehicleExists = true
                        vehicleData.ref.updateChildren(result.toMap())
                            .addOnSuccessListener {
                                Toast.makeText(this@ChangeVehicleActivity, "Vehicle updated successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@ChangeVehicleActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@ChangeVehicleActivity, "Failed to update Vehicle", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                if (!userVehicleExists) {
                    // Jika data kendaraan untuk UID tidak ditemukan, tambahkan data baru
                    database.push().setValue(result)
                        .addOnSuccessListener {
                            Toast.makeText(this@ChangeVehicleActivity, "Vehicle added successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ChangeVehicleActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@ChangeVehicleActivity, "Failed to add Vehicle", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChangeVehicleActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}