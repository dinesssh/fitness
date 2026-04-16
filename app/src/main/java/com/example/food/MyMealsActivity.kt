package com.example.food

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyMealsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_meals)

        db = DatabaseHelper(this)

        setupBottomNavigation()
        setupClickListeners()
        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_nutrition
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_nutrition
        bottomNav.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.nav_home -> Intent(this, MainActivity::class.java)
                R.id.nav_workouts -> Intent(this, WorkoutActivity::class.java)
                R.id.nav_profile -> Intent(this, ProfileActivity::class.java)
                else -> null
            }
            intent?.let {
                val options = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
                startActivity(it, options.toBundle())
                if (item.itemId == R.id.nav_home) finish()
                true
            } ?: false
        }
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnAddBreakfast).setOnClickListener { it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click)); openAddMeal("Breakfast") }
        findViewById<ImageView>(R.id.btnAddLunch).setOnClickListener { it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click)); openAddMeal("Lunch") }
        findViewById<ImageView>(R.id.btnAddDinner).setOnClickListener { it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click)); openAddMeal("Dinner") }
        findViewById<ImageView>(R.id.btnAddSnacks).setOnClickListener { it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click)); openAddMeal("Snacks") }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddWaterQuick).setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))
            db.addWater(250)
            updateDashboard()
            Toast.makeText(this, "250ml Added! 💧", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAddMeal(type: String) {
        val intent = Intent(this, NutritionDetailActivity::class.java)
        intent.putExtra("MEAL_TYPE", type)
        startActivity(intent)
    }

    private fun updateDashboard() {
        val cursorProfile = db.getProfile()
        var calorieGoal = 2200
        if (cursorProfile != null && cursorProfile.moveToFirst()) {
            val goalStr = cursorProfile.getString(cursorProfile.getColumnIndexOrThrow("goal"))
            calorieGoal = if (goalStr == "Weight Loss") 1800 else 2500
            cursorProfile.close()
        }

        val cursorMeals = db.getTodayMeals()
        var totalCal = 0
        var totalPro = 0.0
        var totalCarb = 0.0
        var totalFat = 0.0

        val bItems = findViewById<LinearLayout>(R.id.breakfastItems).apply { removeAllViews() }
        val lItems = findViewById<LinearLayout>(R.id.lunchItems).apply { removeAllViews() }
        val dItems = findViewById<LinearLayout>(R.id.dinnerItems).apply { removeAllViews() }
        val sItems = findViewById<LinearLayout>(R.id.snacksItems).apply { removeAllViews() }

        var bCal = 0; var lCal = 0; var dCal = 0; var sCal = 0

        if (cursorMeals != null) {
            while (cursorMeals.moveToNext()) {
                val id = cursorMeals.getInt(cursorMeals.getColumnIndexOrThrow("id"))
                val name = cursorMeals.getString(cursorMeals.getColumnIndexOrThrow("meal_name"))
                val cal = cursorMeals.getInt(cursorMeals.getColumnIndexOrThrow("calories"))
                val pro = cursorMeals.getDouble(cursorMeals.getColumnIndexOrThrow("protein"))
                val carb = cursorMeals.getDouble(cursorMeals.getColumnIndexOrThrow("carbs"))
                val fat = cursorMeals.getDouble(cursorMeals.getColumnIndexOrThrow("fat"))
                val type = cursorMeals.getString(cursorMeals.getColumnIndexOrThrow("meal_type"))

                totalCal += cal; totalPro += pro; totalCarb += carb; totalFat += fat

                val itemRow = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, null)
                itemRow.findViewById<TextView>(android.R.id.text1).apply {
                    text = name; setTextColor(ContextCompat.getColor(context, R.color.app_text_primary))
                }
                itemRow.findViewById<TextView>(android.R.id.text2).apply {
                    text = "$cal kcal"; setTextColor(ContextCompat.getColor(context, R.color.app_text_secondary))
                }
                itemRow.setOnLongClickListener {
                    db.deleteMeal(id); updateDashboard()
                    Toast.makeText(this, "Meal Removed", Toast.LENGTH_SHORT).show(); true
                }

                when (type) {
                    "Breakfast" -> { bItems.addView(itemRow); bCal += cal }
                    "Lunch" -> { lItems.addView(itemRow); lCal += cal }
                    "Dinner" -> { dItems.addView(itemRow); dCal += cal }
                    "Snacks" -> { sItems.addView(itemRow); sCal += cal }
                }
            }
            cursorMeals.close()
        }

        findViewById<TextView>(R.id.tvTotalCalories).text = "$totalCal / $calorieGoal kcal"
        val pbTotal = findViewById<ProgressBar>(R.id.pbTotalCalories)
        pbTotal.max = calorieGoal
        animateProgressBar(pbTotal, totalCal)

        findViewById<TextView>(R.id.tvProtein).text = "${totalPro.toInt()}g"
        val pbPro = findViewById<ProgressBar>(R.id.pbProtein)
        pbPro.max = 150
        animateProgressBar(pbPro, totalPro.toInt())
        
        findViewById<TextView>(R.id.tvCarbs).text = "${totalCarb.toInt()}g"
        val pbCarb = findViewById<ProgressBar>(R.id.pbCarbs)
        pbCarb.max = 300
        animateProgressBar(pbCarb, totalCarb.toInt())

        findViewById<TextView>(R.id.tvFats).text = "${totalFat.toInt()}g"
        val pbFat = findViewById<ProgressBar>(R.id.pbFats)
        pbFat.max = 100
        animateProgressBar(pbFat, totalFat.toInt())

        findViewById<TextView>(R.id.tvBreakfastCal).text = "$bCal kcal"
        findViewById<TextView>(R.id.tvLunchCal).text = "$lCal kcal"
        findViewById<TextView>(R.id.tvDinnerCal).text = "$dCal kcal"
        findViewById<TextView>(R.id.tvSnacksCal).text = "$sCal kcal"

        val water = db.getTodayWater()
        findViewById<TextView>(R.id.tvWaterRatio).text = "${water / 1000.0}L / 3L"
    }

    private fun animateProgressBar(progressBar: ProgressBar, toProgress: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, toProgress).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}