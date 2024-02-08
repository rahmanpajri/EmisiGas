package com.jri.emisigas.auth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.jri.emisigas.MainActivity
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        progressDialog = ProgressDialog(this, com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        binding.toLogin.setOnClickListener {
            showLoading()
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            hideLoading()
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

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun register(){
        val fullName = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        showLoading()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){ task ->
            hideLoading()
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
                Toast.makeText(this, "Register Failed, Check your Connection", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object{
        private const val TAG = "RegisterActivity"
    }
}