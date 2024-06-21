@file:Suppress("DEPRECATION")

package com.jri.emisigas.home

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.AvgActivity
import com.jri.emisigas.R
import com.jri.emisigas.databinding.FragmentHomeBinding
import com.jri.emisigas.jenis.JenisBbActivity
import com.jri.emisigas.result.Result
import com.jri.emisigas.tips.TipsActivity
import com.jri.emisigas.vehicle.ChangeVehicleActivity
import com.jri.emisigas.vehicle.Vehicle
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var imageProgressBar: ImageView
    private lateinit var progressDialog: ProgressDialog
    val binding get() = _binding!!

  @SuppressLint("UseCompatLoadingForDrawables")
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

      _binding = FragmentHomeBinding.inflate(inflater, container, false)
      val view = binding.root

      auth = FirebaseAuth.getInstance()
      progressDialog = ProgressDialog(requireActivity(), com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
      progressDialog.setMessage("Loading...")
      progressDialog.setCancelable(false)
      progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))
      imageProgressBar = binding.background

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

      binding.avgConsumption.setOnClickListener {
          val intent = Intent(requireContext(), AvgActivity::class.java)
          startActivity(intent)
      }

      if(auth.currentUser != null){
          showProfile()
      }else{
          Toast.makeText(requireContext(), "Please restart the application", Toast.LENGTH_SHORT).show()
      }

      return view
  }

    private fun showLoading() {
        progressDialog.show()
        imageProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressDialog.dismiss()
        imageProgressBar.visibility = View.GONE
    }

    private fun showProfile(){
        showLoading()
        val user = auth.currentUser
        val db = FirebaseDatabase.getInstance()
        if(user != null){
            val userRef = db.reference.child("users").child(user.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoading()
                    if(snapshot.exists()){
                        binding.name.text = snapshot.child("fullName").getValue(String::class.java)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                    Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
                }
            })

            val resultRef = db.reference.child("result")
            resultRef.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val formatter = SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                        var latestData: Result? = null
                        var latestDateTime: Date? = null
                        var totalCO2 = 0.0
                        var totalCH4 = 0.0
                        var totalN2O = 0.0
                        var totalDistance = 0.0
                        var dataCount = 0

                        for (data in snapshot.children) {
                            val result = data.getValue(Result::class.java)
                            if (result != null) {
                                if (result.user_id == user.uid) {
                                    val dateTime = formatter.parse(result.date)
                                    if (dateTime != null) {
                                        if (latestDateTime == null || dateTime.after(latestDateTime)) {
                                            latestDateTime = dateTime
                                            latestData = result
                                        }
                                    }

                                    // Check for empty strings before parsing
                                    val resultCO2 = result.result.takeIf { it.isNotEmpty() }?.toDoubleOrNull() ?: 0.0
                                    val resultCH4 = result.result_CH4.takeIf { it.isNotEmpty() }?.toDoubleOrNull() ?: 0.0
                                    val resultN2O = result.result_N2O.takeIf { it.isNotEmpty() }?.toDoubleOrNull() ?: 0.0
                                    val resultDistance = result.distance.takeIf { it.isNotEmpty() }?.toDoubleOrNull() ?: 0.0

                                    totalCO2 += resultCO2
                                    totalCH4 += resultCH4
                                    totalN2O += resultN2O
                                    totalDistance += resultDistance
                                    dataCount++
                                }
                            }
                        }
                        if (latestData != null) {
                            binding.distance.text = latestData.distance + " km"
                            binding.consumption.text = latestData.result + " kg"
                            binding.consumptionCh4.text = latestData.result_CH4 + " kg"
                            binding.consumptionN2o.text = latestData.result_N2O + " kg"
                        } else {
                            binding.distance.text = "0.0 km"
                            binding.consumption.text = "0.0 kg CO2"
                            binding.consumptionCh4.text = "0.0 kg CH4"
                            binding.consumptionN2o.text = "0.0 kg N2O"
                        }

                        val averageCO2 = if (dataCount > 0) totalCO2 / dataCount else 0.0
                        val averageDistance = if (dataCount > 0) totalDistance / dataCount else 0.0
                        val averageCH4 = if (dataCount > 0) totalCH4 / dataCount else 0.0
                        val averageN2O = if (dataCount > 0) totalN2O / dataCount else 0.0
                        val decimalFormat = DecimalFormat("#.####")
                        decimalFormat.roundingMode = RoundingMode.DOWN
                        val decimalFormat2 = DecimalFormat("#.########")
                        decimalFormat2.roundingMode = RoundingMode.DOWN
                        val formattedResultCO2 = decimalFormat.format(averageCO2)
                        val formattedResultCH4 = decimalFormat2.format(averageCH4)
                        val formattedResultN2O = decimalFormat2.format(averageN2O)
                        val formattedDistance = decimalFormat.format(averageDistance)
                        binding.avgCount.text = "$formattedResultCO2 kg"
                        binding.avgDistance.text = "$formattedDistance km"
                        binding.avgCh4Count.text = "$formattedResultCH4 kg"
                        binding.avgN2oCount.text = "$formattedResultN2O kg"

                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
                }
            })

            val vehicleRef = db.reference.child("vehicle")
            vehicleRef.addListenerForSingleValueEvent(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isUserVehicleExist = false
                    for (data in snapshot.children) {
                        val vehicle = data.getValue(Vehicle::class.java)
                        if (vehicle != null) {
                            if (vehicle.user_id == user.uid) {
                                isUserVehicleExist = true
                                binding.carType.text = vehicle.brand
                                binding.capacity.text = vehicle.capacity
                                binding.plate.text = vehicle.plate
                                break
                            }
                        }
                    }
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
}