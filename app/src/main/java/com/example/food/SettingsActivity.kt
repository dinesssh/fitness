package com.example.food

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Unit Selection
        val toggleUnit = findViewById<MaterialButtonToggleGroup>(R.id.toggleUnit)
        val currentUnit = prefs.getString("unit", "kg")
        toggleUnit.check(if (currentUnit == "kg") R.id.btnKg else R.id.btnLbs)

        toggleUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val unit = if (checkedId == R.id.btnKg) "kg" else "lbs"
                prefs.edit().putString("unit", unit).apply()
                Toast.makeText(this, "Units updated to $unit", Toast.LENGTH_SHORT).show()
            }
        }

        // Notifications
        val switchNotifications = findViewById<MaterialSwitch>(R.id.switchNotifications)
        switchNotifications.isChecked = prefs.getBoolean("notifications", true)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        // Reset Data
        findViewById<MaterialButton>(R.id.btnResetData).setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Reset All Data?")
                .setMessage("This will permanently delete all your progress, meals, and workouts. This action cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    val db = DatabaseHelper(this)
                    db.writableDatabase.execSQL("DELETE FROM meals")
                    db.writableDatabase.execSQL("DELETE FROM water")
                    db.writableDatabase.execSQL("DELETE FROM activity")
                    db.writableDatabase.execSQL("DELETE FROM workout_history")
                    db.writableDatabase.execSQL("DELETE FROM user_profile")
                    
                    prefs.edit().clear().apply()
                    
                    Toast.makeText(this, "All data has been reset", Toast.LENGTH_LONG).show()
                    
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}