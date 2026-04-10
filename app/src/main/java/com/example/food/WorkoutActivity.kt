package com.example.food

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class WorkoutActivity : AppCompatActivity() {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        db = DatabaseHelper(this)
        viewModel = ViewModelProvider(this)[WorkoutViewModel::class.java]

        setupBottomNavigation()
        setupUI()
        setupRecyclerViews()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_workouts
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MyMealsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_workouts -> true
                R.id.nav_nutrition -> {
                    startActivity(Intent(this, NutritionDetailActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun setupUI() {
        findViewById<MaterialButton>(R.id.btnStartWorkout).setOnClickListener {
            startActivity(Intent(this, WorkoutSelectionActivity::class.java))
        }

        val streak = db.getStreak()
        findViewById<TextView>(R.id.tvWorkoutStreak).text = "🔥 $streak-day streak"
    }

    private fun setupRecyclerViews() {
        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)
        
        val cursor = db.getWorkoutHistory()
        val historyList = mutableListOf<WorkoutHistory>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                historyList.add(WorkoutHistory(
                    cursor.getString(cursor.getColumnIndexOrThrow("workout_type")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("duration_min")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("calories_burned")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date"))
                ))
            }
            cursor.close()
        }

        historyList.forEach { history ->
            val view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, historyContainer, false)
            view.findViewById<TextView>(android.R.id.text1).apply {
                text = "${history.type} • ${history.duration} min"
                setTextColor(ContextCompat.getColor(context, R.color.gh_text))
            }
            view.findViewById<TextView>(android.R.id.text2).apply {
                text = "${history.date} • ${history.calories} kcal burned"
                setTextColor(ContextCompat.getColor(context, R.color.gh_text_muted))
            }
            historyContainer.addView(view)
        }
    }
}