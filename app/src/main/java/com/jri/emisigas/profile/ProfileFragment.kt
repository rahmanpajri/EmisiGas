package com.jri.emisigas.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jri.emisigas.auth.LoginActivity
import com.jri.emisigas.databinding.FragmentProfileBinding
import com.jri.emisigas.vehicle.ChangeVehicleActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val profileViewModel =
            ViewModelProvider(this)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        return root
  }

    private fun logout() {
        Firebase.auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
            _binding = null
        }
    }