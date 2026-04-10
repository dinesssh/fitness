package com.example.food

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class WorkoutSessionActivity : AppCompatActivity() {

    private var seconds = 0
    private var running = true
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var tvTimer: TextView
    private lateinit var tvExerciseName: TextView
    private lateinit var tvRestTimer: TextView
    private lateinit var etSets: TextInputEditText
    private lateinit var etReps: TextInputEditText
    private lateinit var etWeight: TextInputEditText

    private var exercises = listOf("Bench Press", "Incline DB Press", "Cable Flyes", "Pushups")
    private var currentExerciseIndex = 0
    private var workoutType = "Chest Day"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_session)

        workoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "General Workout"
        setupExercisesByType(workoutType)

        tvTimer = findViewById(R.id.tvTimer)
        tvExerciseName = findViewById(R.id.tvExerciseName)
        tvRestTimer = findViewById(R.id.tvRestTimer)
        etSets = findViewById(R.id.etSets)
        etReps = findViewById(R.id.etReps)
        etWeight = findViewById(R.id.etWeight)

        updateExerciseUI()
        runTimer()

        findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            startRestTimer()
            if (currentExerciseIndex < exercises.size - 1) {
                currentExerciseIndex++
                updateExerciseUI()
                clearInputs()
            } else {
                Toast.makeText(this, "Workout completed!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnFinish).setOnClickListener {
            finishWorkout()
        }
    }

    private fun setupExercisesByType(type: String) {
        exercises = when (type) {
            "Chest Day" -> listOf("Bench Press", "Incline DB Press", "Cable Flyes", "Pushups")
            "Back Day" -> listOf("Deadlifts", "Lat Pulldowns", "Bent Over Rows", "Pullups")
            "Arm Day" -> listOf("Bicep Curls", "Hammer Curls", "Tricep Pushdowns", "Skull Crushers")
            "Leg Day" -> listOf("Squats", "Leg Press", "Leg Extensions", "Calf Raises")
            "Shoulder Day" -> listOf("Overhead Press", "Lateral Raises", "Front Raises", "Rear Delt Flyes")
            "Cardio" -> listOf("Running", "Cycling", "Jump Rope", "Burpees")
            else -> listOf("Pushups", "Squats", "Plank", "Jumping Jacks")
        }
    }

    private fun updateExerciseUI() {
        tvExerciseName.text = exercises[currentExerciseIndex]
    }

    private fun clearInputs() {
        etSets.text?.clear()
        etReps.text?.clear()
        etWeight.text?.clear()
    }

    private fun runTimer() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                val time = String.format("%02d:%02d", minutes, secs)
                tvTimer.text = time
                if (running) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun startRestTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secRemaining = millisUntilFinished / 1000
                tvRestTimer.text = "Rest: 00:${String.format("%02d", secRemaining % 60)}"
            }

            override fun onFinish() {
                tvRestTimer.text = "Rest: 01:00"
                Toast.makeText(this@WorkoutSessionActivity, "Rest Over! Go!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun finishWorkout() {
        val db = DatabaseHelper(this)
        val duration = seconds / 60
        db.logWorkout(workoutType, duration, duration * 8)
        Toast.makeText(this, "$workoutType Saved!", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}