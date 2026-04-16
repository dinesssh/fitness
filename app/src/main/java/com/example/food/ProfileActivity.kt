package com.example.food

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = DatabaseHelper(this)
        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 800
        fadeIn.fillAfter = true
        scrollView.startAnimation(fadeIn)
        scrollView.alpha = 1f

        setupNavigation()
        setupBottomNavigation()
        loadProfileData()
    }

    private fun loadProfileData() {
        // Safe access to database and views to prevent crashes
        val cursor = db.getProfile()
        cursor?.use {
            if (it.moveToFirst()) {
                val name = "User" // Placeholder as schema doesn't have name
                findViewById<TextView>(R.id.tvProfileName)?.text = name
            }
        }

        // Lifetime Stats calculation
        val historyCursor = db.getWorkoutHistory()
        var totalWorkouts = 0
        var totalBurned = 0
        
        historyCursor?.use {
            totalWorkouts = it.count
            val burnedIndex = it.getColumnIndex("calories_burned")
            while (it.moveToNext()) {
                if (burnedIndex != -1) {
                    totalBurned += it.getInt(burnedIndex)
                }
            }
        }
        
        findViewById<TextView>(R.id.tvStatsPreview)?.text = "$totalWorkouts Workouts • $totalBurned kcal burned"

        // Achievements Logic
        val streak = db.getStreak()
        val achievements = mutableListOf<String>()
        if (totalWorkouts > 0) achievements.add("🥇 First Workout")
        if (streak >= 3) achievements.add("🔥 3-Day Streak")
        if (streak >= 7) achievements.add("🏆 7-Day Streak")
        
        val achievementText = if (achievements.isEmpty()) "Keep going! trophies await." else achievements.joinToString(" • ")
        findViewById<TextView>(R.id.tvAchievementsPreview)?.text = achievementText
    }

    private fun setupNavigation() {
        findViewById<MaterialButton>(R.id.btnEditProfile)?.setOnClickListener {
            startActivity(Intent(this, AssessmentActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardStats)?.setOnClickListener {
            startActivity(Intent(this, CalorieStatsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardAchievements)?.setOnClickListener {
            // Navigate to achievements detail if exists
        }

        findViewById<MaterialCardView>(R.id.cardSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardAccount)?.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = R.id.nav_profile
        bottomNav?.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.nav_home -> Intent(this, MainActivity::class.java)
                R.id.nav_workouts -> Intent(this, WorkoutActivity::class.java)
                R.id.nav_nutrition -> Intent(this, MyMealsActivity::class.java)
                else -> null
            }
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(it)
                true
            } ?: false
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_profile
    }
}