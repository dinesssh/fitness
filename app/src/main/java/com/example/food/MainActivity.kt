package com.example.food

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
        findViewById<MaterialButton>(R.id.btnQuickLogMeal).setOnClickListener {
            startActivity(Intent(this, MyMealsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnQuickAddWater).setOnClickListener {
            showWaterDialog()
        }

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_workouts -> startActivity(Intent(this, WorkoutActivity::class.java))
                R.id.nav_nutrition -> startActivity(Intent(this, MyMealsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            }
            true
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
        db.addWater(amount)
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // 1. Calories
        val todayMeals = db.getTodayMeals()
        var consumed = 0
        if (todayMeals != null) {
            while (todayMeals.moveToNext()) {
                consumed += todayMeals.getInt(todayMeals.getColumnIndexOrThrow("calories"))
            }
            todayMeals.close()
        }
        val calGoal = 2200
        findViewById<TextView>(R.id.tvCalorieRatio).text = "$consumed / $calGoal"
        findViewById<ProgressBar>(R.id.pbCalories).apply {
            max = calGoal
            progress = consumed
            // Color logic: Red if exceeded
            if (consumed > calGoal) {
                progressTintList = android.content.res.ColorStateList.valueOf(Color.RED)
            } else {
                progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#00BCD4"))
            }
        }
        findViewById<TextView>(R.id.tvCalorieLeft).apply {
            if (consumed > calGoal) {
                text = "${consumed - calGoal} kcal over"
                setTextColor(Color.RED)
            } else {
                text = "${calGoal - consumed} kcal left"
                setTextColor(Color.parseColor("#4CAF50"))
            }
        }

        // 2. Water
        val waterIntake = db.getTodayWater()
        val waterGoal = 3000
        findViewById<TextView>(R.id.tvWater).text = "${waterIntake / 1000.0}L / ${waterGoal / 1000.0}L"
        findViewById<ProgressBar>(R.id.pbWater).apply {
            max = waterGoal
            progress = waterIntake
        }

        // 3. Activity
        val activityCursor = db.getTodayActivity()
        var steps = 0
        var workoutMin = 0
        if (activityCursor != null && activityCursor.moveToFirst()) {
            steps = activityCursor.getInt(activityCursor.getColumnIndexOrThrow("steps"))
            workoutMin = activityCursor.getInt(activityCursor.getColumnIndexOrThrow("workout_minutes"))
            activityCursor.close()
        }
        findViewById<TextView>(R.id.tvSteps).text = "$steps Steps"
        findViewById<TextView>(R.id.tvWorkoutMin).text = "$workoutMin min workout"

        // 4. Fitness Score
        val calProgress = (consumed.toFloat() / calGoal).coerceAtMost(1f)
        val waterProgress = (waterIntake.toFloat() / waterGoal).coerceAtMost(1f)
        val activityProgress = ((steps / 10000f) + (workoutMin / 60f)) / 2f
        val totalScore = ((calProgress + waterProgress + activityProgress) / 3f * 100).toInt().coerceIn(0, 100)
        
        findViewById<TextView>(R.id.tvFitnessScore).text = "$totalScore"
        findViewById<ProgressBar>(R.id.pbFitnessScore).progress = totalScore

        setupChart()
    }

    private fun setupChart() {
        val chart = findViewById<LineChart>(R.id.calorieChart)
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dbSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // Ensure 7 days are always present
        val weeklyData = mutableMapOf<String, Int>()
        val last7Days = mutableListOf<String>()
        
        for (i in 0 until 7) {
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
            labels.add(sdf.format(date!!))
            entries.add(Entry(index.toFloat(), weeklyData[dateStr]!!.toFloat()))
        }

        val primaryColor = Color.parseColor("#00BCD4")
        val dataSet = LineDataSet(entries, "Calories").apply {
            color = primaryColor
            setCircleColor(primaryColor)
            lineWidth = 3f
            circleRadius = 5f
            circleHoleRadius = 2.5f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = primaryColor
            fillAlpha = 40
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
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#E0F7FA")
                textColor = Color.parseColor("#607D8B")
                yOffset = 10f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0F7FA")
                setDrawAxisLine(false)
                textColor = Color.parseColor("#607D8B")
                xOffset = 10f
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            extraBottomOffset = 10f
            animateXY(1000, 1000)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
}