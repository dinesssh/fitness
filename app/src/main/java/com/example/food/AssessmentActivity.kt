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

        val usernameInput = findViewById<TextInputEditText>(R.id.usernameInput)
        val ageInput = findViewById<TextInputEditText>(R.id.ageInput)
        val heightInput = findViewById<TextInputEditText>(R.id.heightInput)
        val weightInput = findViewById<TextInputEditText>(R.id.weightInput)
        val genderGroup = findViewById<RadioGroup>(R.id.genderGroup)
        val activityLevelDropdown = findViewById<AutoCompleteTextView>(R.id.activityLevelDropdown)
        val continueButton = findViewById<MaterialButton>(R.id.continueButton)

        val activityLevels = arrayOf("Sedentary", "Moderate", "Active", "Very Active")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityLevels)
        activityLevelDropdown.setAdapter(adapter)

        // Load existing data from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "")
        val savedAge = sharedPref.getInt("age", -1)
        val savedHeight = sharedPref.getInt("height", -1)
        val savedWeight = sharedPref.getFloat("weight", -1f)
        val savedGender = sharedPref.getString("gender", "")
        val savedActivityLevel = sharedPref.getString("activity_level", "")

        if (!savedUsername.isNullOrEmpty()) usernameInput.setText(savedUsername)
        if (savedAge != -1) ageInput.setText(savedAge.toString())
        if (savedHeight != -1) heightInput.setText(savedHeight.toString())
        if (savedWeight != -1f) weightInput.setText(savedWeight.toString())
        
        if (!savedGender.isNullOrEmpty()) {
            for (i in 0 until genderGroup.childCount) {
                val rb = genderGroup.getChildAt(i) as? RadioButton
                if (rb?.text?.toString()?.equals(savedGender, ignoreCase = true) == true) {
                    rb.isChecked = true
                    break
                }
            }
        }
        
        if (!savedActivityLevel.isNullOrEmpty()) {
            activityLevelDropdown.setText(savedActivityLevel, false)
        }

        continueButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val ageStr = ageInput.text.toString().trim()
            val heightStr = heightInput.text.toString().trim()
            val weightStr = weightInput.text.toString().trim()
            val activityLevel = activityLevelDropdown.text.toString().trim()

            val selectedGenderId = genderGroup.checkedRadioButtonId
            val gender = if (selectedGenderId != -1) {
                findViewById<RadioButton>(selectedGenderId).text.toString()
            } else ""

            if (username.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || gender.isEmpty() || activityLevel.isEmpty()) {
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

            try {
                // Save all data to SharedPreferences for easy retrieval
                sharedPref.edit().apply {
                    putString("username", username)
                    putInt("age", age)
                    putInt("height", height)
                    putFloat("weight", weight.toFloat())
                    putString("gender", gender)
                    putString("activity_level", activityLevel)
                    apply()
                }

                db.saveProfile(age, weight, height, gender, "Weight Loss", activityLevel)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("AssessmentActivity", "Error saving profile", e)
            }
        }
    }
}