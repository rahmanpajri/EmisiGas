package com.jri.emisigas.auth

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
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

        binding.forgotPassword.setOnClickListener {
            forgotPassword()
        }
    }

    private fun forgotPassword() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
        val userEmail = view.findViewById<EditText>(R.id.editBox)

        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<Button>(R.id.btnReset).setOnClickListener {
            compareEmail(userEmail)
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        if(dialog.window != null){
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog.show()
    }

    private fun compareEmail(email: EditText){
        if(email.text.toString().isEmpty()){
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
            return
        }
        auth.sendPasswordResetEmail(email.text.toString()).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(this, "Check Your Email", Toast.LENGTH_SHORT).show()
            }
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
                    val verification = auth.currentUser?.isEmailVerified
                    if(verification == true){
                        Toast.makeText(this, "Success, Welcome to CarbonTrack", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, "Please verify your Email!", Toast.LENGTH_SHORT).show()
                    }
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