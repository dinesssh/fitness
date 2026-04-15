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

        // Dropdown setup
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

            // Validation
            if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageStr.toIntOrNull()
            val height = heightStr.toIntOrNull()
            val weight = weightStr.toDoubleOrNull()

            if (age == null || height == null || weight == null) {
                Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age <= 0 || height <= 0 || weight <= 0) {
                Toast.makeText(this, "Values must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val result = db.saveProfile(age, weight, height, gender, "Weight Loss")

                if (result == -1L) {
                    Toast.makeText(this, "Database insert failed", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MyMealsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AssessmentActivity", "Error saving profile", e)
                Toast.makeText(this, "DB Error: " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}