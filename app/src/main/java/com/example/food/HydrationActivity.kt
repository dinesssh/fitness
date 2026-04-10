package com.example.food

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton

class HydrationActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var tvCurrentWater: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var waterFillView: View
    private val waterGoal = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration)

        db = DatabaseHelper(this)

        tvCurrentWater = findViewById(R.id.currentWater)
        tvPercentage = findViewById(R.id.tvPercentage)
        waterFillView = findViewById(R.id.waterFillView)
        val btnAddWater = findViewById<MaterialButton>(R.id.btnAddWater)
        val btnBack = findViewById<ImageView>(R.id.backButton)

        btnBack.setOnClickListener { finish() }

        btnAddWater.setOnClickListener {
            db.addWater(250)
            updateUI()
            Toast.makeText(this, "250ml Added!", Toast.LENGTH_SHORT).show()
        }

        updateUI()
    }

    private fun updateUI() {
        val todayWater = db.getTodayWater()
        tvCurrentWater.text = "$todayWater ml"

        val percentage = (todayWater.toFloat() / waterGoal.toFloat() * 100).toInt().coerceAtMost(100)
        tvPercentage.text = "$percentage%"

        // Update the visual fill
        val params = waterFillView.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentHeight = (todayWater.toFloat() / waterGoal.toFloat()).coerceAtMost(1.0f)
        waterFillView.layoutParams = params
    }
}