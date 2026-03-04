package com.example.food

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MyMealsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_meals)

        // Start Foreground Service
        val serviceIntent = Intent(this, FoodReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // --- Feature Navigation ---

        // 1. Nearby Food (Location Feature)
        findViewById<MaterialButton>(R.id.nearbyFoodButton).setOnClickListener {
            checkLocationPermissions()
        }

        // 2. Nutrition Detail
        findViewById<MaterialButton>(R.id.nutritionButton).setOnClickListener {
            startActivity(Intent(this, NutritionDetailActivity::class.java))
        }

        // 3. Calorie Stats
        findViewById<MaterialButton>(R.id.statsButton).setOnClickListener {
            startActivity(Intent(this, CalorieStatsActivity::class.java))
        }

        // 4. Hydration Tracker
        findViewById<MaterialButton>(R.id.hydrationButton).setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }

        // 5. Diet Report (SMS Feature)
        findViewById<MaterialButton>(R.id.shareReportButton).setOnClickListener {
            startActivity(Intent(this, DietReportActivity::class.java))
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val city = addr.locality ?: "Unknown"
                        val fullAddress = addr.getAddressLine(0)
                        
                        val locationMsg = "Location: $city\nLat: ${location.latitude}\nLong: ${location.longitude}\nAddress: $fullAddress"
                        Toast.makeText(this, locationMsg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Enable GPS to find location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }
}