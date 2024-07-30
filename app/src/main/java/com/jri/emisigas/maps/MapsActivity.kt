@file:Suppress("DEPRECATION")

package com.jri.emisigas.maps

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityMapsBinding
import com.jri.emisigas.result.Result
import com.jri.emisigas.result.ResultActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var auth: FirebaseAuth
    private var isTracking = false
    private lateinit var locationCallback: LocationCallback
    private lateinit var progressDialog: ProgressDialog
    private var allLatLng = ArrayList<LatLng>()
    private var boundsBuilder = LatLngBounds.Builder()
    private var totalDistance = 0.0
    private var totalIdleDistance = 0.0

    // Constants for idle estimation
    private val IDLE_SPEED_THRESHOLD = 6.5 // km/jam
    private val IDLE_SPEED_ESTIMATE = 6.5 // km/jam
    private var idleStartTime: Long = 0
    private var isIdleStarted = false

    private var totalIdleTime = 0L

    private var totalFuelConsumption = 0.0
    private var totalFuelConsumptionIdle = 0.0

    private var totalMovingEmissionCO2 = 0.0
    private var totalIdleEmissionCO2 = 0.0

    private var totalMovingEmissionTier2CO2 = 0.0
    private var totalIdleEmissionTier2CO2 = 0.0

    private var totalMovingEmissionCH4 = 0.0
    private var totalIdleEmissionCH4 = 0.0

    private var totalMovingEmissionN2O = 0.0
    private var totalIdleEmissionN2O = 0.0

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        progressDialog = ProgressDialog(this, com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()
        createLocationCallback()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        createLocationRequest()

        binding.btnStart.setOnClickListener {
            if (!isTracking) {
                clearMaps()
                updateTrackingStatus(true)
                startLocationUpdates()
            } else {
                updateTrackingStatus(false)
                stopLocationUpdates()

                val totalEmissionCO2 = totalIdleEmissionCO2 + totalMovingEmissionCO2
                val fuelEfficiencyCO2 = getFullEfficiencyCO2()
                val totalEmissionTier2CO2 = totalIdleEmissionTier2CO2 + totalMovingEmissionTier2CO2
                val totalDistance = totalIdleDistance + totalDistance
                val totalFuelConsumption = totalFuelConsumptionIdle + totalFuelConsumption

                val formattedTotalEmissionCO2 = String.format("%.5f", totalEmissionCO2)

                val totalEmissionCH4 = totalIdleEmissionCH4 + totalMovingEmissionCH4

                val totalEmissionN20 = totalIdleEmissionN2O + totalMovingEmissionN2O

                val formattedTotalDistance = String.format("%.5f", totalDistance)

//                Log.d(TAG, "Total Emission Tier 1 & 3: $totalEmissionCH4 kg CH4")
//                Log.d(TAG, "Total Emission Tier 1 & 3: $totalEmissionN20 kg N2O")
                Log.d(TAG, "Total Emission Tier 2: $totalEmissionTier2CO2 kg CO2")
                Log.d(TAG, "Total Distance: $totalDistance km")
                Log.d(TAG, "Fuel Consumption: $totalFuelConsumption")
                Log.d(TAG, "Emission Factor CO2: $fuelEfficiencyCO2")

                addResult()

                val intent = Intent(this@MapsActivity, ResultActivity::class.java)
                intent.putExtra("TOTAL_EMISSION_CO2", formattedTotalEmissionCO2)
                intent.putExtra("TOTAL_EMISSION_CH4", totalEmissionCH4)
                intent.putExtra("TOTAL_EMISSION_N2O", totalEmissionN20)
                intent.putExtra("TOTAL_DISTANCE", formattedTotalDistance)
                startActivity(intent)

                finish()
            }
        }

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
    }

    private fun addResult(){
        val uid = auth.currentUser!!.uid
        val jenisId = intent.getStringExtra("jenis_id") ?: ""
        val tahunId = intent.getStringExtra("tahun_id") ?: ""
        val formattedTotalEmissionCO2 = String.format("%.4f", totalIdleEmissionCO2 + totalMovingEmissionCO2)
        val formattedTotalEmissionCH4 = String.format("%.8f", totalIdleEmissionCH4 + totalMovingEmissionCH4)
        val formattedTotalEmissionN2O = String.format("%.8f", totalIdleEmissionN2O + totalMovingEmissionN2O)
        val formattedTotalDistance = String.format("%.4f", totalIdleDistance + totalDistance)
        val date = SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val result = Result(date, formattedTotalDistance, jenisId, formattedTotalEmissionCO2, formattedTotalEmissionCH4, formattedTotalEmissionN2O, tahunId, uid)

        val database = FirebaseDatabase.getInstance().reference.child("result").push()

        database.setValue(result)
            .addOnSuccessListener {
                Toast.makeText(this, "Result added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add Result, Check your Connection", Toast.LENGTH_LONG).show()
            }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                else -> {
                    // No location access granted.
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker(location)
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Location is Not Found. Try Again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showStartMarker(location: Location) {
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(startLocation)
                .title(getString(R.string.start_point))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 17f))
    }

    private val resolutionLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> Log.i(TAG, "onActivityResult: All location settings are satisfied.")
                RESULT_CANCELED ->
                    Toast.makeText(
                        this@MapsActivity,
                        "You must Turn On GPS for Using This App!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(IntentSenderRequest.Builder(exception.resolution).build())
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@MapsActivity, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun updateTrackingStatus(newStatus: Boolean) {
        isTracking = newStatus

        if (isTracking) {
            binding.btnStart.text = getString(R.string.stop_trip)
        } else {
            binding.btnStart.text = getString(R.string.start_trip)
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    Log.d(TAG, "onLocationResult: " + location.latitude + "," + location.longitude)

                    val lastLatLng = LatLng(location.latitude, location.longitude)

                    if(location.speed > IDLE_SPEED_THRESHOLD){
                        allLatLng.add(lastLatLng)
                        mMap.addPolyline(
                            PolylineOptions()
                                .color(R.color.md_theme_primaryContainer)
                                .width(10f)
                                .addAll(allLatLng)
                        )
                    }

                    // Hitung jarak tempuh dari titik sebelumnya
                    if (allLatLng.size >= 2 && location.speed > IDLE_SPEED_THRESHOLD) {
                        val distance = calculateDistance(allLatLng[allLatLng.size - 2], lastLatLng)
                        totalDistance += distance

                        val fuelEfficiencyCO2 = getFullEfficiencyCO2()
                        val fuelEfficiencyCH4 = getFullEfficiencyCH4()
                        val fuelEfficiencyN2O = getFullEfficiencyN2O()

                        Log.d(TAG, "fuelEfficiency: $fuelEfficiencyCO2")

                        val movingEmissionCO2 = distance * fuelEfficiencyCO2 * fuelConsumption
                        totalMovingEmissionCO2 += movingEmissionCO2
                        Log.d(TAG, "Moving Emission: $movingEmissionCO2 kg CO2")

                        val movingEmissionTier1CO2 = fuelEfficiencyCO2 * fuelConsumption
                        totalFuelConsumption += fuelConsumption
                        totalMovingEmissionTier2CO2 += movingEmissionTier1CO2

                        val movingEmissionCH4 = distance * fuelEfficiencyCH4 * fuelConsumption
                        totalMovingEmissionCH4 += movingEmissionCH4

                        val movingEmissionN2O = distance * fuelEfficiencyN2O * fuelConsumption
                        totalMovingEmissionN2O += movingEmissionN2O

                        val idleTimeInSeconds = calculateIdleTime(location)
                        totalIdleTime += idleTimeInSeconds
                        val idleDistance = calculateIdleDistance(idleTimeInSeconds)
                        totalIdleDistance += idleDistance

                        val idleEmissionCO2 = idleDistance * fuelEfficiencyCO2 * fuelConsumptionIdle
                        totalIdleEmissionCO2 += idleEmissionCO2
                        Log.d(TAG, "Idle Emission: $idleEmissionCO2 kg CO2")

                        val idleEmissionTier1CO2 = fuelEfficiencyCO2 * fuelConsumptionIdle
                        totalFuelConsumptionIdle += fuelConsumptionIdle
                        totalIdleEmissionTier2CO2 += idleEmissionTier1CO2

                        val idleEmissionCH4 = idleDistance * fuelEfficiencyCH4 * fuelConsumptionIdle
                        totalIdleEmissionCH4 += idleEmissionCH4

                        val idleEmissionN2O = idleDistance * fuelEfficiencyN2O * fuelConsumptionIdle
                        totalIdleEmissionN2O += idleEmissionN2O

                    }

                    boundsBuilder.include(lastLatLng)
                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
                }
            }
        }
    }

    private fun getFullEfficiencyCO2(): Double{
        val jenisId = intent.getStringExtra("jenis_id")
        val tahunId = intent.getStringExtra("tahun_id")

        return when {
            jenisId == "4" && tahunId == "1" -> 74800.0
            jenisId == "4" && tahunId == "2" -> 74100.0
            jenisId == "4" && tahunId == "3" -> 72600.0
            jenisId != "4" && tahunId == "1" -> 73000.0
            jenisId != "4" && tahunId == "2" -> 69300.0
            jenisId != "4" && tahunId == "3" -> 67500.0
            else -> 0.0
        }
    }

    private fun getFullEfficiencyCH4(): Double{
        val jenisId = intent.getStringExtra("jenis_id")
        val tahunId = intent.getStringExtra("tahun_id")

        return when {
            jenisId == "4" && tahunId == "1" -> 9.5
            jenisId == "4" && tahunId == "2" -> 3.9
            jenisId == "4" && tahunId == "3" -> 1.6
            else -> 0.0
        }
    }

    private fun getFullEfficiencyN2O(): Double{
        val jenisId = intent.getStringExtra("jenis_id")
        val tahunId = intent.getStringExtra("tahun_id")

        return when {
            jenisId == "4" && tahunId == "1" -> 12.0
            jenisId == "4" && tahunId == "2" -> 3.9
            jenisId == "4" && tahunId == "3" -> 1.3
            else -> 0.0
        }
    }

    private fun calculateIdleTime(location: Location): Long {
        val isCurrentlyIdle = location.speed < IDLE_SPEED_THRESHOLD

        if (isCurrentlyIdle && !isIdleStarted) {
            // Jika kendaraan baru saja menjadi idle, catat waktu mulai idle
            idleStartTime = System.currentTimeMillis()
            isIdleStarted = true
        } else if (!isCurrentlyIdle && isIdleStarted) {
            // Jika kendaraan telah bergerak setelah idle, reset waktu idle
            idleStartTime = 0
            isIdleStarted = false
        }

        // Hitung waktu idle dalam detik
        val currentTime = System.currentTimeMillis()
        val idleTimeInMillis = currentTime - idleStartTime
        val idleTimeInSeconds = idleTimeInMillis / 1000

        return if (isIdleStarted) {
            idleTimeInSeconds
        } else {
            0
        }
    }

    private fun calculateIdleDistance(idleTimeInSeconds: Long): Double {
        // Menggunakan estimasi kecepatan idle untuk menghitung jarak tempuh saat idle
        val idleSpeed = IDLE_SPEED_ESTIMATE
        return idleSpeed * idleTimeInSeconds.toDouble() / 3600.0 // Konversi ke kilometer
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e(TAG, "Error: " + exception.message)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isTracking) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun clearMaps() {
        mMap.clear()
        allLatLng.clear()
        boundsBuilder = LatLngBounds.Builder()
    }

    private fun calculateDistance(startLatLng: LatLng, endLatLng: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(
            startLatLng.latitude, startLatLng.longitude,
            endLatLng.latitude, endLatLng.longitude, result
        )
        return result[0].toDouble() / 1000.0 // Mengonversi meter menjadi kilometer
    }

    companion object {
        private const val TAG = "MapsActivity"
        private const val fuelConsumption = 4.95e-6 // TJ/km
        private const val fuelConsumptionIdle = 5.0e-6
    }
}
