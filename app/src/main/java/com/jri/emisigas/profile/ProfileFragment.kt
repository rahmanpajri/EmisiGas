package com.jri.emisigas.profile

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.jri.emisigas.R
import com.jri.emisigas.auth.LoginActivity
import com.jri.emisigas.databinding.FragmentProfileBinding
import com.jri.emisigas.vehicle.ChangeVehicleActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view= binding.root

        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(requireContext(), com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        showProfile()

        binding.profileButton.setOnClickListener {
            val intent = Intent(requireContext(), ChangeProfileActivity::class.java)
            startActivity(intent)
        }

        binding.vehicle.setOnClickListener {
            val intent = Intent(requireContext(), ChangeVehicleActivity::class.java)
            startActivity(intent)
        }

        binding.logout.setOnClickListener {
          logout()
        }

        return view
  }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun showProfile(){
        val user = auth.currentUser
        val db = FirebaseDatabase.getInstance()
        showLoading()
        if(user != null){
            val userRef = db.reference.child("users").child(user.uid)
            hideLoading()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        binding.fullName.text = snapshot.child("fullName").getValue(String::class.java)
                        binding.email.text = snapshot.child("email").getValue(String::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error, Check your Connection", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun logout() {
        showLoading()
        Firebase.auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        Toast.makeText(requireContext(), "Logout Success", Toast.LENGTH_SHORT).show()
        startActivity(intent)
        hideLoading()
    }
    }