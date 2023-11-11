package com.jri.emisigas.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.jri.emisigas.MainActivity
import com.jri.emisigas.databinding.ActivityRegisterBinding
import com.jri.emisigas.maps.MapsActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.toLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener {
            register()
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if(currentUser != null){
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun register(){
        val fullName = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){ task ->
            if(task.isSuccessful){
                val userId = auth.currentUser!!.uid

                val database = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                val userData = HashMap<String, Any>()
                userData["uid"] = userId
                userData["fullName"] = fullName
                userData["email"] = email

                database.updateChildren(userData).addOnCompleteListener{
                    if(it.isSuccessful){
                        Log.d(TAG, "createUser:Success")
                        Toast.makeText(this, "User Created", Toast.LENGTH_SHORT).show()
                        Firebase.auth.signOut()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }else{
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        private const val TAG = "RegisterActivity"
    }
}