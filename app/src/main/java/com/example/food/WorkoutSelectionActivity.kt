package com.example.food

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class WorkoutSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_selection)

        findViewById<MaterialCardView>(R.id.cardChestDay).setOnClickListener { startWorkout("Chest Day") }
        findViewById<MaterialCardView>(R.id.cardBackDay).setOnClickListener { startWorkout("Back Day") }
        findViewById<MaterialCardView>(R.id.cardArmDay).setOnClickListener { startWorkout("Arm Day") }
        findViewById<MaterialCardView>(R.id.cardLegDay).setOnClickListener { startWorkout("Leg Day") }
        findViewById<MaterialCardView>(R.id.cardShoulderDay).setOnClickListener { startWorkout("Shoulder Day") }
        findViewById<MaterialCardView>(R.id.cardCardio).setOnClickListener { startWorkout("Cardio") }
    }

    private fun startWorkout(type: String) {
        val intent = Intent(this, WorkoutSessionActivity::class.java)
        intent.putExtra("WORKOUT_TYPE", type)
        startActivity(intent)
        finish()
    }
}