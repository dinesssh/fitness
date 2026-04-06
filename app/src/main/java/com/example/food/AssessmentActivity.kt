package com.example.food

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AssessmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        val db = DatabaseHelper(this)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val weightValue = findViewById<TextView>(R.id.weightValue)
        val continueButton = findViewById<MaterialButton>(R.id.continueButton)
        
        // Note: Assuming these IDs exist in your actual layout or adding logic for them
        // Gender and Goal selection would typically be RadioGroups or Cards in a real-world app.
        // For this upgrade, I'll use default values if UI elements aren't fully visible in provided snippets.

        continueButton.setOnClickListener {
            val age = ageInput.text.toString()
            val weight = weightValue.text.toString().filter { it.isDigit() || it == '.' }

            if (age.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Save profile to SQLite
                db.saveProfile(
                    age.toInt(),
                    weight.toDouble(),
                    "Male", // Default or from RadioGroup
                    "Weight Loss" // Default or from Goal selection
                )
                
                startActivity(Intent(this, MyMealsActivity::class.java))
                finish()
            }
        }
    }
}