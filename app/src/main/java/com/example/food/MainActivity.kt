package com.example.food

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        setupUI()
        setupBottomNavigation()
    }

    private fun setupUI() {
        // Retrieve and display username
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("username", "User") ?: "User"
        tvGreeting.text = getString(R.string.greeting_format, username)

        findViewById<MaterialButton>(R.id.btnQuickLogMeal).setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))
            val intent = Intent(this, MyMealsActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }

        findViewById<MaterialButton>(R.id.btnQuickAddWater).setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))
            showWaterDialog()
        }

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.nav_workouts -> Intent(this, WorkoutActivity::class.java)
                R.id.nav_nutrition -> Intent(this, MyMealsActivity::class.java)
                R.id.nav_profile -> Intent(this, ProfileActivity::class.java)
                else -> null
            }
            intent?.let {
                val options = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
                startActivity(it, options.toBundle())
                true
            } ?: false
        }
    }

    private fun showWaterDialog() {
        val options = arrayOf("100ml", "250ml", "500ml", "Custom")
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Add Water Intake")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addWaterAndUpdate(100)
                    1 -> addWaterAndUpdate(250)
                    2 -> addWaterAndUpdate(500)
                    3 -> showCustomWaterDialog()
                }
            }
            .show()
    }

    private fun showCustomWaterDialog() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "e.g. 750"
        }
        val container = android.widget.FrameLayout(this).apply {
            val params = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = 64
                rightMargin = 64
                topMargin = 16
                bottomMargin = 16
            }
            addView(input, params)
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Custom Amount (ml)")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toIntOrNull() ?: 0
                addWaterAndUpdate(amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addWaterAndUpdate(amount: Int) {
        if (amount > 0) {
            db.addWater(amount)
            Toast.makeText(this, "Water intake updated 💧", Toast.LENGTH_SHORT).show()
            loadDashboardData()
        }
    }

    private fun loadDashboardData() {
        val todayMeals = db.getTodayMeals()
        var consumed = 0
        if (todayMeals != null) {
            while (todayMeals.moveToNext()) {
                consumed += todayMeals.getInt(todayMeals.getColumnIndexOrThrow("calories"))
            }
            todayMeals.close()
        }
        
        val calGoal = db.getDailyCalorieGoal()

        findViewById<TextView>(R.id.tvCalorieRatio).text = getString(R.string.calorie_ratio, consumed, calGoal)
        val pbCalories = findViewById<ProgressBar>(R.id.pbCalories)
        pbCalories.max = calGoal
        animateProgressBar(pbCalories, consumed)
        
        val color = when {
            consumed > calGoal -> ContextCompat.getColor(this, R.color.app_error)
            consumed > calGoal * 0.9 -> ContextCompat.getColor(this, R.color.app_warning)
            else -> ContextCompat.getColor(this, R.color.app_primary)
        }
        pbCalories.progressTintList = ColorStateList.valueOf(color)

        findViewById<TextView>(R.id.tvCalorieLeft).apply {
            if (consumed > calGoal) {
                text = getString(R.string.calorie_over, consumed - calGoal)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.app_error))
            } else {
                text = getString(R.string.calorie_left, calGoal - consumed)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.app_success))
            }
        }

        val waterIntake = db.getTodayWater()
        val waterGoal = 3000
        findViewById<TextView>(R.id.tvWater).text = getString(R.string.water_ratio, waterIntake / 1000.0, waterGoal / 1000.0)
        val pbWater = findViewById<ProgressBar>(R.id.pbWater)
        pbWater.max = waterGoal
        animateProgressBar(pbWater, waterIntake)
        pbWater.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_primary))

        val activityCursor = db.getTodayActivity()
        var steps = 0
        var workoutMin = 0
        if (activityCursor != null && activityCursor.moveToFirst()) {
            val stepsIndex = activityCursor.getColumnIndex("steps")
            val workoutIndex = activityCursor.getColumnIndex("workout_minutes")
            if (stepsIndex != -1) steps = activityCursor.getInt(stepsIndex)
            if (workoutIndex != -1) workoutMin = activityCursor.getInt(workoutIndex)
            activityCursor.close()
        }
        findViewById<TextView>(R.id.tvSteps).text = getString(R.string.steps_format, steps)
        findViewById<TextView>(R.id.tvWorkoutMin).text = getString(R.string.workout_min_format, workoutMin)

        val calProgress = if (calGoal > 0) (consumed.toFloat() / calGoal).coerceAtMost(1f) else 0f
        val waterProgress = (waterIntake.toFloat() / waterGoal).coerceAtMost(1f)
        val activityProgress = ((steps / 10000f) + (workoutMin / 60f)) / 2f
        val totalScore = ((calProgress + waterProgress + activityProgress) / 3f * 100).toInt().coerceIn(0, 100)
        
        findViewById<TextView>(R.id.tvFitnessScore).text = totalScore.toString()
        animateProgressBar(findViewById(R.id.pbFitnessScore), totalScore)

        updateInsights(consumed, calGoal, waterIntake, waterGoal, totalScore)
        setupChart()
    }

    private fun updateInsights(consumed: Int, calGoal: Int, waterIntake: Int, waterGoal: Int, score: Int) {
        val insightCard = findViewById<View>(R.id.insightsCard) ?: return
        val insightText = findViewById<TextView>(R.id.tvInsightMessage) ?: return
        val streak = db.getStreak()

        val insight = when {
            streak > 3 -> "You're on fire! $streak-day streak! 🔥"
            waterIntake < waterGoal * 0.5 -> "Stay hydrated! Time for a glass of water. 💧"
            consumed < calGoal * 0.4 -> "Calories are low. Time for a healthy snack? 🍎"
            score > 80 -> "Excellent progress today! Keep it up! 🌟"
            else -> null
        }

        if (insight != null) {
            insightText.text = insight
            insightCard.visibility = View.VISIBLE
        } else {
            insightCard.visibility = View.GONE
        }
    }

    private fun animateProgressBar(progressBar: ProgressBar, toProgress: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, toProgress).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun setupChart() {
        val chart = findViewById<LineChart>(R.id.calorieChart) ?: return
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dbSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        val weeklyData = mutableMapOf<String, Int>()
        val last7Days = mutableListOf<String>()
        
        repeat(7) {
            val dateStr = dbSdf.format(calendar.time)
            last7Days.add(0, dateStr)
            weeklyData[dateStr] = 0
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val cursor = db.getWeeklyCalories()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val date = cursor.getString(0)
                val cal = cursor.getInt(1)
                if (weeklyData.containsKey(date)) {
                    weeklyData[date] = cal
                }
            }
            cursor.close()
        }

        last7Days.forEachIndexed { index, dateStr ->
            val date = dbSdf.parse(dateStr)
            labels.add(if (date != null) sdf.format(date) else "")
            entries.add(Entry(index.toFloat(), weeklyData[dateStr]?.toFloat() ?: 0f))
        }

        val primaryColor = ContextCompat.getColor(this, R.color.app_primary)
        val dataSet = LineDataSet(entries, "Calories").apply {
            color = primaryColor
            setCircleColor(primaryColor)
            lineWidth = 4f
            circleRadius = 6f
            circleHoleRadius = 3f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.chart_fill_gradient)
            fillAlpha = 80
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(this@MainActivity, R.color.app_text_secondary)
                textSize = 12f
                yOffset = 15f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = "#E0F7FA".toColorInt()
                gridLineWidth = 1f
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(this@MainActivity, R.color.app_text_secondary)
                textSize = 12f
                xOffset = 15f
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            extraBottomOffset = 20f
            animateXY(1000, 1000)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
}