package com.jri.emisigas

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.databinding.ActivityAvgBinding
import com.jri.emisigas.history.HistoryAdapter
import com.jri.emisigas.result.Result
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AvgActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAvgBinding

    private lateinit var selectedStartDate: Calendar
    private lateinit var selectedEndDate: Calendar
    private lateinit var rvHistory: RecyclerView

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private val list = ArrayList<Result>()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnStartDate = binding.startDateButton
        val btnEndDate = binding.endDateButton

        progressDialog = ProgressDialog(this, com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        selectedStartDate = Calendar.getInstance()
        selectedEndDate = Calendar.getInstance()

        btnStartDate.setOnClickListener {
            showDatePickerForStartDate()
        }
        btnEndDate.setOnClickListener {
            showDatePickerForEndDate()
        }

        rvHistory = binding.rvHistory
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.setHasFixedSize(true)

        binding.filterButton.setOnClickListener {
            getFilteredData()
        }
    }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun showDatePickerForStartDate() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedStartDate.set(Calendar.YEAR, year)
                selectedStartDate.set(Calendar.MONTH, month)
                selectedStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                selectedStartDate.set(Calendar.HOUR_OF_DAY, 0)
                selectedStartDate.set(Calendar.MINUTE, 0)
                selectedStartDate.set(Calendar.SECOND, 0)

                updateStartDate()
            },
            selectedStartDate.get(Calendar.YEAR),
            selectedStartDate.get(Calendar.MONTH),
            selectedStartDate.get(Calendar.DAY_OF_MONTH),


        )
        datePicker.show()
    }

    private fun updateStartDate() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.startDateText.text = sdf.format(selectedStartDate.time)
    }

    private fun showDatePickerForEndDate() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedEndDate.set(Calendar.YEAR, year)
                selectedEndDate.set(Calendar.MONTH, month)
                selectedEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                selectedEndDate.set(Calendar.HOUR_OF_DAY, 23)
                selectedEndDate.set(Calendar.MINUTE, 59)
                selectedEndDate.set(Calendar.SECOND, 59)

                updateEndDate()
            },
            selectedEndDate.get(Calendar.YEAR),
            selectedEndDate.get(Calendar.MONTH),
            selectedEndDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateEndDate() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.endDateText.text = sdf.format(selectedEndDate.time)
    }

    private fun getFilteredData(){
        showLoading()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        dbRef = database.reference.child("result")

        dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                hideLoading()
                if(snapshot.exists()){
                    list.clear()
                    var totalResult = 0.0
                    var totalDistance = 0.0
                    var dataCount = 0
                    for (resultSnapshot in snapshot.children){
                        val result = resultSnapshot.getValue(Result::class.java)
                        if (result != null) {
                            if (result.user_id == user?.uid) {
                                val sdf = SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                                val date = sdf.parse(result.date)

                                if (date != null && date.time >= selectedStartDate.timeInMillis &&
                                    date.time <= selectedEndDate.timeInMillis
                                ) {
                                    totalResult += result.result.toDouble()
                                    totalDistance += result.distance.toDouble()
                                    dataCount++
                                    list.add(result)
                                }
                            }
                        }
                    }
                    val averageResult = if (dataCount > 0) totalResult / dataCount else 0.0
                    val averageDistance = if (dataCount > 0) totalDistance / dataCount else 0.0
                    val decimalFormat = DecimalFormat("#.####")
                    decimalFormat.roundingMode = RoundingMode.DOWN
                    val formattedResult = decimalFormat.format(averageResult)
                    val formattedDistance = decimalFormat.format(averageDistance)
                    val formattedResultSum = decimalFormat.format(totalResult)
                    val formattedDistanceSum = decimalFormat.format(totalDistance)
                    binding.avgResult.text = "$formattedResult kg CO2"
                    binding.avgDistance.text = "$formattedDistance km"
                    binding.sumResult.text = "$formattedResultSum kg CO2"
                    binding.sumDistance.text = "$formattedDistanceSum km"
                    rvHistory.adapter = HistoryAdapter(list)

                }else{
                    Toast.makeText(this@AvgActivity, "Data is Null", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                hideLoading()
                Toast.makeText(this@AvgActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}