package com.example.food

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class NutritionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_detail)

        val db = DatabaseHelper(this)
        
        val etName = findViewById<EditText>(R.id.etMealName)
        val etCal = findViewById<EditText>(R.id.etCalories)
        val etPro = findViewById<EditText>(R.id.etProtein)
        val etCarb = findViewById<EditText>(R.id.etCarbs)
        val etFat = findViewById<EditText>(R.id.etFat)
        val btnSave = findViewById<MaterialButton>(R.id.addMealButton)
        val btnBack = findViewById<ImageView>(R.id.backButton)

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val cal = etCal.text.toString()
            val pro = etPro.text.toString()
            val carb = etCarb.text.toString()
            val fat = etFat.text.toString()

            if (name.isEmpty() || cal.isEmpty() || pro.isEmpty() || carb.isEmpty() || fat.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                db.addMeal(name, cal.toInt(), pro.toDouble(), carb.toDouble(), fat.toDouble())
                Toast.makeText(this, "Meal Saved Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}