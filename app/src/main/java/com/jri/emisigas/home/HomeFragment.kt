package com.jri.emisigas.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.databinding.FragmentHomeBinding
import com.jri.emisigas.jenis.JenisBbActivity
import com.jri.emisigas.tips.TipsActivity
import com.jri.emisigas.vehicle.ChangeVehicleActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
      val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

      _binding = FragmentHomeBinding.inflate(inflater, container, false)
      val view = binding.root

      binding.button.setOnClickListener {
          val intent = Intent(requireContext(), JenisBbActivity::class.java)
          startActivity(intent)
      }

      binding.tips.setOnClickListener {
          val intent = Intent(requireContext(), TipsActivity::class.java)
          startActivity(intent)
      }

      binding.editCar.setOnClickListener {
          val intent = Intent(requireContext(), ChangeVehicleActivity::class.java)
          startActivity(intent)
      }

      showProfile()

      homeViewModel.text.observe(viewLifecycleOwner) {

      }
    return view
  }

    private fun showProfile(){
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val db = FirebaseDatabase.getInstance()

        if(user != null){
            val userRef = db.reference.child("users").child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        binding.name.text = snapshot.child("fullName").getValue(String::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}