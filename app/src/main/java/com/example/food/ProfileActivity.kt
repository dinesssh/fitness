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
            // Navigate to Account Details page instead of direct logout
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.nav_home -> Intent(this, MainActivity::class.java)
                R.id.nav_workouts -> Intent(this, WorkoutActivity::class.java)
                R.id.nav_nutrition -> Intent(this, MyMealsActivity::class.java)
                R.id.nav_profile -> null
                else -> null
            }
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(it)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_profile
    }
}