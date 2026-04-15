package com.example.food

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
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
        val btnEditProfile = findViewById<MaterialButton>(R.id.btnEditProfile)
        val cardStats = findViewById<MaterialCardView>(R.id.cardStats)
        val cardAchievements = findViewById<MaterialCardView>(R.id.cardAchievements)
        val cardSettings = findViewById<MaterialCardView>(R.id.cardSettings)
        val cardAccount = findViewById<MaterialCardView>(R.id.cardAccount)

        btnEditProfile.setOnClickListener {
            // Navigate to AssessmentActivity to edit profile data
            val intent = Intent(this, AssessmentActivity::class.java)
            startActivity(intent)
        }

        cardStats.setOnClickListener {
            val intent = Intent(this, CalorieStatsActivity::class.java)
            startActivity(intent)
        }

        cardAchievements.setOnClickListener {
            Toast.makeText(this, "Achievements feature coming soon! 🏆", Toast.LENGTH_SHORT).show()
        }

        cardSettings.setOnClickListener {
            Toast.makeText(this, "Settings feature coming soon! ⚙️", Toast.LENGTH_SHORT).show()
        }

        cardAccount.setOnClickListener {
            // For now, logout functionality
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