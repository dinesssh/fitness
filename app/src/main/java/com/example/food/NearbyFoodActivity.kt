package com.example.food

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.button.MaterialButton
import java.util.Locale

class NearbyFoodActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvFullAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_food)

        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvCity = findViewById(R.id.tvCity)
        tvFullAddress = findViewById(R.id.tvFullAddress)
        val btnGetLocation = findViewById<MaterialButton>(R.id.btnGetLocation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnGetLocation.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show()

        // Use getCurrentLocation instead of lastLocation for better accuracy on first try
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val long = location.longitude

                    tvLatitude.text = "Latitude: $lat"
                    tvLongitude.text = "Longitude: $long"

                    updateAddress(lat, long)
                } else {
                    Toast.makeText(this, "Unable to get location. Ensure GPS is ON.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Location error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAddress(lat: Double, long: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, long, 1) { addresses ->
                runOnUiThread {
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        tvCity.text = "City: ${address.locality ?: "Unknown"}"
                        tvFullAddress.text = "Full Address: ${address.getAddressLine(0)}"
                    }
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, long, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    tvCity.text = "City: ${address.locality ?: "Unknown"}"
                    tvFullAddress.text = "Full Address: ${address.getAddressLine(0)}"
                }
            } catch (e: Exception) {
                tvFullAddress.text = "Geocoder error: ${e.message}"
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}