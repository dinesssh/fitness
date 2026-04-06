package com.example.food

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_meals)

        db = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Start Foreground Service
        val serviceIntent = Intent(this, FoodReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        setupNavigation()
        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun setupNavigation() {
        findViewById<MaterialButton>(R.id.nearbyFoodButton).setOnClickListener { checkLocationPermissions() }
        findViewById<MaterialButton>(R.id.nutritionButton).setOnClickListener {
            startActivity(Intent(this, NutritionDetailActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.hydrationButton).setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.shareReportButton).setOnClickListener {
            startActivity(Intent(this, DietReportActivity::class.java))
        }
    }

    private fun updateDashboard() {
        val cursorProfile = db.getProfile()
        var calorieGoal = 2000 // Default
        
        if (cursorProfile != null && cursorProfile.moveToFirst()) {
            val goal = cursorProfile.getString(cursorProfile.getColumnIndexOrThrow("goal"))
            calorieGoal = if (goal == "Weight Loss") 1600 else 2400
            cursorProfile.close()
        }

        val cursorMeals = db.getTodayMeals()
        var totalCal = 0
        var totalPro = 0.0
        var totalCarb = 0.0
        var totalFat = 0.0

        val mealListContainer = findViewById<LinearLayout>(R.id.mealListContainer)
        mealListContainer.removeAllViews()

        if (cursorMeals != null) {
            while (cursorMeals.moveToNext()) {
                val id = cursorMeals.getInt(cursorMeals.getColumnIndexOrThrow("id"))
                val name = cursorMeals.getString(cursorMeals.getColumnIndexOrThrow("meal_name"))
                val cal = cursorMeals.getInt(cursorMeals.getColumnIndexOrThrow("calories"))
                val pro = cursorMeals.getDouble(cursorMeals.getColumnIndexOrThrow("protein"))
                val carb = cursorMeals.getDouble(cursorMeals.getColumnIndexOrThrow("carbs"))
                val fat = cursorMeals.getDouble(cursorMeals.getColumnIndexOrThrow("fat"))

                totalCal += cal
                totalPro += pro
                totalCarb += carb
                totalFat += fat

                // Simple horizontal view for each meal
                val textView = TextView(this)
                textView.text = "• $name ($cal kcal)"
                textView.setPadding(0, 8, 0, 8)
                textView.setOnLongClickListener {
                    db.deleteMeal(id)
                    updateDashboard()
                    Toast.makeText(this, "Meal Deleted", Toast.LENGTH_SHORT).show()
                    true
                }
                mealListContainer.addView(textView)
            }
            cursorMeals.close()
        }

        findViewById<TextView>(R.id.tvCaloriesValue).text = "$totalCal / $calorieGoal kcal"
        val pbCalories = findViewById<ProgressBar>(R.id.pbCalories)
        pbCalories.max = calorieGoal
        pbCalories.progress = totalCal
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
                    val fullAddress = addresses?.get(0)?.getAddressLine(0) ?: "Unknown"
                    Toast.makeText(this, "Location: $fullAddress", Toast.LENGTH_LONG).show()
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