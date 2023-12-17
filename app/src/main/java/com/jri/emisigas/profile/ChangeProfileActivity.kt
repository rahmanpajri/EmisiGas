package com.jri.emisigas.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.MainActivity
import com.jri.emisigas.databinding.ActivityChangeProfileBinding

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeProfileBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val db = FirebaseDatabase.getInstance()

        if(user != null){
            val userRef = db.reference.child("users").child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val fullName = snapshot.child("fullName").getValue(String::class.java)
                        binding.fullNameEditTextLayout.editText?.setText(fullName)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChangeProfileActivity, "Database Error", Toast.LENGTH_SHORT).show()
                }

            })
        }

        binding.changeButton.setOnClickListener {
            val newFullName = binding.fullNameEditText.text.toString().trim()
            val newPassword = binding.passwordEditText.text.toString().trim()
            validateAndUpdateProfile(newFullName, newPassword)
        }
    }

    private fun validateAndUpdateProfile(newFullName: String, newPassword: String) {
        if (newFullName.isEmpty() && newPassword.isEmpty()) {
            Toast.makeText(this, "Nama lengkap dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else if (newFullName.isEmpty()) {
            Toast.makeText(this, "Nama lengkap tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else if (newPassword.isEmpty()) {
            Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
        } else {
            updateProfile(newFullName, newPassword)
        }
    }

    private fun updateProfile(newFullName: String, newPassword: String) {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val db: FirebaseDatabase = FirebaseDatabase.getInstance()
            val userRef: DatabaseReference = db.reference.child("users").child(user.uid)

            // Update fullName pada Realtime Database
            userRef.child("fullName").setValue(newFullName)
                .addOnSuccessListener {
                    if (newPassword.isNotEmpty()) {
                        currentUser.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    // Gagal update fullName
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }
}