package com.example.food

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class NutritionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_detail)

        val db = DatabaseHelper(this)
        
        val mealTypeDropdown = findViewById<AutoCompleteTextView>(R.id.mealTypeDropdown)
        val etName = findViewById<TextInputEditText>(R.id.etMealName)
        val etCal = findViewById<TextInputEditText>(R.id.etCalories)
        val etPro = findViewById<TextInputEditText>(R.id.etProtein)
        val etCarb = findViewById<TextInputEditText>(R.id.etCarbs)
        val etFat = findViewById<TextInputEditText>(R.id.etFat)
        val etFiber = findViewById<TextInputEditText>(R.id.etFiber)
        val btnSave = findViewById<MaterialButton>(R.id.addMealButton)
        val btnBack = findViewById<ImageView>(R.id.backButton)

        // Setup Meal Type Dropdown
        val mealTypes = arrayOf("Breakfast", "Lunch", "Dinner", "Snacks")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mealTypes)
        mealTypeDropdown.setAdapter(adapter)
        
        // Auto-select based on intent or default
        val intentType = intent.getStringExtra("MEAL_TYPE") ?: "Breakfast"
        mealTypeDropdown.setText(intentType, false)

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val type = mealTypeDropdown.text.toString()
            val name = etName.text.toString().trim()
            val calStr = etCal.text.toString().trim()
            val proStr = etPro.text.toString().trim()
            val carbStr = etCarb.text.toString().trim()
            val fatStr = etFat.text.toString().trim()
            val fiberStr = etFiber.text.toString().trim()

            if (name.isEmpty() || calStr.isEmpty() || proStr.isEmpty() || carbStr.isEmpty() || fatStr.isEmpty() || fiberStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    db.addMeal(
                        name,
                        calStr.toInt(),
                        proStr.toDouble(),
                        carbStr.toDouble(),
                        fatStr.toDouble(),
                        fiberStr.toDouble(),
                        type
                    )
                    Toast.makeText(this, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving meal. Check numeric inputs.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}