package com.example.food

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AssessmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        val db = DatabaseHelper(this)
        val ageInput = findViewById<TextInputEditText>(R.id.ageInput)
        val heightInput = findViewById<TextInputEditText>(R.id.heightInput)
        val weightInput = findViewById<TextInputEditText>(R.id.weightInput)
        val genderGroup = findViewById<RadioGroup>(R.id.genderGroup)
        val activityLevelDropdown = findViewById<AutoCompleteTextView>(R.id.activityLevelDropdown)
        val continueButton = findViewById<MaterialButton>(R.id.continueButton)

        // Setup Activity Level Dropdown
        val activityLevels = arrayOf("Sedentary", "Moderate", "Active", "Very Active")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityLevels)
        activityLevelDropdown.setAdapter(adapter)
        
        continueButton.setOnClickListener {
            val ageStr = ageInput.text.toString().trim()
            val heightStr = heightInput.text.toString().trim()
            val weightStr = weightInput.text.toString().trim()
            
            val selectedGenderId = genderGroup.checkedRadioButtonId
            val gender = if (selectedGenderId != -1) {
                findViewById<RadioButton>(selectedGenderId).text.toString()
            } else ""

            if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val age = ageStr.toInt()
                    val height = heightStr.toInt()
                    val weight = weightStr.toDouble()

                    if (age <= 0 || height <= 0 || weight <= 0) {
                        Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Save profile to SQLite
                    db.saveProfile(
                        age,
                        weight,
                        height,
                        gender,
                        "Weight Loss" // Default or can be added to UI
                    )
                    
                    val intent = Intent(this, MyMealsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("AssessmentActivity", "Error saving profile: ${e.message}")
                    Toast.makeText(this, "Error saving data. Please check inputs.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}