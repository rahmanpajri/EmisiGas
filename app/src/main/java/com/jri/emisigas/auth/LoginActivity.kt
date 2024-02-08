package com.jri.emisigas.auth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jri.emisigas.MainActivity
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        progressDialog = ProgressDialog(this, com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.toRegister.setOnClickListener {
            showLoading()
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            hideLoading()
        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if(currentUser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun login(){
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Complete Data", Toast.LENGTH_SHORT).show()
        }else{
            showLoading()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                hideLoading()
                if(task.isSuccessful){
                    Toast.makeText(this, "Success, Welcome to CarbonTrack", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Login Failed, Check your Connection", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                hideLoading()
                Toast.makeText(this, "Login Failed, Check your Connection", Toast.LENGTH_LONG).show()
            }
        }
    }
}