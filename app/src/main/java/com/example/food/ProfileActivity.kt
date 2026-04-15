package com.example.food

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        
        // Smooth fade-in animation
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 800
        fadeIn.fillAfter = true
        scrollView.startAnimation(fadeIn)
        scrollView.alpha = 1f

        setupNavigation()
        setupBottomNavigation()
    }

    private fun setupNavigation() {
        val cardStats = findViewById<MaterialCardView>(R.id.cardStats)
        val cardAchievements = findViewById<MaterialCardView>(R.id.cardAchievements)
        val cardSettings = findViewById<MaterialCardView>(R.id.cardSettings)
        val cardAccount = findViewById<MaterialCardView>(R.id.cardAccount)

        cardStats.setOnClickListener {
            // Navigate to StatsActivity (using CalorieStatsActivity as it exists)
            val intent = Intent(this, CalorieStatsActivity::class.java)
            startActivity(intent)
        }

        cardAchievements.setOnClickListener {
            // Placeholder: Navigate to AchievementsActivity
            // startActivity(Intent(this, AchievementsActivity::class.java))
        }

        cardSettings.setOnClickListener {
            // Placeholder: Navigate to SettingsActivity
            // startActivity(Intent(this, SettingsActivity::class.java))
        }

        cardAccount.setOnClickListener {
            // Placeholder: Navigate to AccountActivity or handle Logout
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_workouts -> {
                    startActivity(Intent(this, WorkoutActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_nutrition -> {
                    startActivity(Intent(this, MyMealsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_profile
    }
}