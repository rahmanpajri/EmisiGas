package com.jri.emisigas.maps

import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityMapsBinding
import com.jri.emisigas.result.ResultActivity
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var isTracking = false
    private lateinit var locationCallback: LocationCallback
    private var allLatLng = ArrayList<LatLng>()
    private var boundsBuilder = LatLngBounds.Builder()
    private var totalDistance = 0.0

    // Constants for idle estimation
    private val IDLE_SPEED_THRESHOLD = 1.0 // km/jam
    private val IDLE_SPEED_ESTIMATE = 5.0 // km/jam
    private var idleStartTime: Long = 0
    private var isIdleStarted = false

    private var totalMovingEmission = 0.0
    private var totalIdleEmission = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                val totalEmission = totalIdleEmission + totalMovingEmission
                Log.d(TAG, "Total Emission: $totalEmission kg CO2")

                // Kirim totalEmission ke ResultActivity
                val intent = Intent(this@MapsActivity, ResultActivity::class.java)
                intent.putExtra("TOTAL_EMISSION", totalEmission)
                intent.putExtra("TOTAL_DISTANCE", totalDistance)
                startActivity(intent)

                // Selesaikan MapsActivity
                finish()
            }
        }

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
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

                    allLatLng.add(lastLatLng)
                    mMap.addPolyline(
                        PolylineOptions()
                            .color(R.color.md_theme_light_primaryContainer)
                            .width(10f)
                            .addAll(allLatLng)
                    )

                    // Hitung jarak tempuh dari titik sebelumnya
                    if (allLatLng.size >= 2) {
                        val distance = calculateDistance(allLatLng[allLatLng.size - 2], lastLatLng)
                        totalDistance += distance
                        Log.d(TAG, "onLocationResult: Distance - $distance, Total Distance - $totalDistance km")

                        val movingEmission = distance * fuelEfficiency * fuelConsumption

                        totalMovingEmission += movingEmission
                        Log.d(TAG, "Moving Emission: $movingEmission kg CO2")

                        // Hitung total emisi
                        val totalEmission = totalMovingEmission
                        Log.d(TAG, "Total Emission: $totalEmission kg CO2")
                    }



                    boundsBuilder.include(lastLatLng)
                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
                }
            }
        }
    }

    private fun calculateIdleTime(location: Location): Long {
        val isCurrentlyIdle = isIdle(location)

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

    private fun isIdle(location: Location): Boolean {
        // Implementasi sesuai dengan kebutuhan aplikasi Anda
        // Misalnya, jika kecepatan kendaraan kurang dari batas tertentu
        return location.speed < IDLE_SPEED_THRESHOLD
    }

    private fun calculateIdleDistance(idleTimeInSeconds: Long): Double {
        // Implementasi sesuai dengan kebutuhan aplikasi Anda
        // Misalnya, menggunakan estimasi kecepatan idle untuk menghitung jarak tempuh saat idle
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
        private const val fuelEfficiency = 69300.0 // kg/TJ
        private const val fuelConsumption = 4.95e-6 // TJ/km
    }
}
