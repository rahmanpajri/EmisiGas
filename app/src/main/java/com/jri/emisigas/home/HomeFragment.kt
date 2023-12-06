package com.jri.emisigas.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.databinding.FragmentHomeBinding
import com.jri.emisigas.jenis.JenisBbActivity
import com.jri.emisigas.result.Result
import com.jri.emisigas.tips.TipsActivity
import com.jri.emisigas.vehicle.ChangeVehicleActivity
import com.jri.emisigas.vehicle.Vehicle
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

      _binding = FragmentHomeBinding.inflate(inflater, container, false)
      val view = binding.root

      auth = FirebaseAuth.getInstance()

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

      return view
  }

    private fun showProfile(){
        val user = auth.currentUser
        val db = FirebaseDatabase.getInstance()

        if(user != null){
            val userRef = db.reference.child("users").child(user.uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        binding.name.text = snapshot.child("fullName").getValue(String::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
                }

            })

            val resultRef = db.reference.child("result")
            resultRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val formatter = SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm:ss", Locale.getDefault())

                        var latestData: Result? = null
                        var latestDateTime: Date? = null
                        var totalResult = 0.0
                        var dataCount = 0

                        for (data in snapshot.children) {
                            val result = data.getValue(Result::class.java)
                            if (result != null) {
                                if (result.user_id == user.uid) {
                                    val dateTime = formatter.parse(result.date)

                                    if (latestDateTime == null || dateTime.after(latestDateTime)) {
                                        latestDateTime = dateTime
                                        latestData = result
                                    }
                                    totalResult += result.result.toDouble()
                                    // Menambah jumlah data
                                    dataCount++
                                }
                            }

                        }

                        if (latestData != null) {
                            binding.distance.text = latestData.distance + " km"
                            binding.consumption.text = latestData.result + " kg CO2"
                        } else {
                            binding.distance.text = "0 km"
                            binding.consumption.text = "0 kg CO2"
                        }

                        val averageResult = if (dataCount > 0) totalResult / dataCount else 0.0
                        val decimalFormat = DecimalFormat("#.####")
                        decimalFormat.roundingMode = RoundingMode.DOWN
                        val formattedResult = decimalFormat.format(averageResult)
                        binding.avgCount.text = "$formattedResult kg CO2"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
                }
            })

            val vehicleRef = db.reference.child("vehicle")
            vehicleRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isUserVehicleExist = false // Flag untuk menandai apakah data user_vehicle ada
                    for (data in snapshot.children) {
                        val vehicle = data.getValue(Vehicle::class.java)
                        if (vehicle != null) {
                            if (vehicle.user_id == user.uid) {
                                isUserVehicleExist = true
                                binding.carType.text = vehicle.brand
                                binding.capacity.text = vehicle.capacity
                                binding.plate.text = vehicle.plate
                                break // Keluar dari loop karena sudah ditemukan data yang cocok
                            }
                        }
                    }

                    // Jika tidak ada data yang cocok, set teks menjadi 'Set Car Type', 'Set Capacity', 'Set Plate'
                    if (!isUserVehicleExist) {
                        binding.carType.text = "Set Car Type"
                        binding.capacity.text = "Set Capacity"
                        binding.plate.text = "Set Plate"
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