package com.example.food

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Start Foreground Service
        val serviceIntent = Intent(this, MealTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setupUI()
        setupBottomNavigation()
        loadDashboardData()
    }

    private fun setupUI() {
        // Quick Action Listeners
        findViewById<MaterialButton>(R.id.btnQuickLogMeal).setOnClickListener {
            startActivity(Intent(this, NutritionDetailActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnQuickStartWorkout).setOnClickListener {
            startActivity(Intent(this, WorkoutSelectionActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnQuickAddWater).setOnClickListener {
            db.addWater(250)
            loadDashboardData()
            Toast.makeText(this, "250ml Water Added! 💧", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            // Future Profile Activity
            Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutActivity::class.java))
                    true
                }
                R.id.nav_nutrition -> {
                    startActivity(Intent(this, MyMealsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Section", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDashboardData() {
        // Dynamic Greeting
        val cursorProfile = db.getProfile()
        if (cursorProfile != null && cursorProfile.moveToFirst()) {
            // Using a hardcoded name as per request "Harsith", or we could fetch if saved
            findViewById<TextView>(R.id.tvGreeting).text = "Good Day, Harsith 👋"
            cursorProfile.close()
        }

        // Calorie Logic
        val todayMeals = db.getTodayMeals()
        var consumed = 0
        if (todayMeals != null) {
            while (todayMeals.moveToNext()) {
                consumed += todayMeals.getInt(todayMeals.getColumnIndexOrThrow("calories"))
            }
            todayMeals.close()
        }

        val goal = 2200
        val left = (goal - consumed).coerceAtLeast(0)
        
        findViewById<TextView>(R.id.tvCalorieRatio).text = "$consumed / $goal kcal"
        findViewById<ProgressBar>(R.id.pbCalories).apply {
            max = goal
            progress = consumed
        }
        findViewById<TextView>(R.id.tvCalorieLeft).text = "$left kcal left"

        // Daily Snapshot Data
        val waterIntake = db.getTodayWater()
        findViewById<TextView>(R.id.tvWater).text = "${waterIntake / 1000.0}L / 3L"
        
        val streak = db.getStreak()
        findViewById<TextView>(R.id.tvStreak).text = "$streak Days 🔥"

        // Smart Insight Message
        val insightMessage = when {
            left > 1000 -> "You are well below your calorie goal. Log your next meal! 🍽️"
            left in 1..500 -> "Almost at your calorie goal. Keep it up! 🎯"
            waterIntake < 2000 -> "Don't forget to hydrate! Drink more water. 💧"
            else -> "You're on track for a great day! Keep going. 🚀"
        }
        findViewById<TextView>(R.id.tvInsightMessage).text = insightMessage
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
        // Ensure bottom nav selection is correct
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home
    }
}