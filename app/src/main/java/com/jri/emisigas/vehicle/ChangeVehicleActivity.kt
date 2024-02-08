package com.jri.emisigas.vehicle

import android.annotation.SuppressLint
import android.app.ProgressDialog
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
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityChangeVehicleBinding

class ChangeVehicleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeVehicleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this, com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        displayVehicleData()

        binding.changeButton.setOnClickListener {
            changeData()
        }
    }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun displayVehicleData() {
        showLoading()
        val uid = auth.currentUser!!.uid
        val database = FirebaseDatabase.getInstance().reference.child("vehicle")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hideLoading()
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
                hideLoading()
            }
        })
    }



    private fun changeData() {
        showLoading()
        val uid = auth.currentUser!!.uid
        val brand = binding.brandEditText.text.toString()
        val plate = binding.plateEditText.text.toString()
        val capacity = binding.machineEditText.text.toString()
        val result = Vehicle(brand, capacity, plate, uid)

        val database = FirebaseDatabase.getInstance().reference.child("vehicle")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hideLoading()
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
                                Toast.makeText(this@ChangeVehicleActivity, "Failed to update, Check your Connection", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                if (!userVehicleExists) {
                    database.push().setValue(result)
                        .addOnSuccessListener {
                            Toast.makeText(this@ChangeVehicleActivity, "Vehicle added successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ChangeVehicleActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@ChangeVehicleActivity, "Failed to add Vehicle, Check your Connection", Toast.LENGTH_LONG).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideLoading()
                Toast.makeText(this@ChangeVehicleActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}