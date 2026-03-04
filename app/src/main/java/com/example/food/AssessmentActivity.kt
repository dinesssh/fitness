package com.example.food

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AssessmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)

        val ageInput = findViewById<EditText>(R.id.ageInput)
        val weightValue = findViewById<TextView>(R.id.weightValue)
        val continueButton = findViewById<MaterialButton>(R.id.continueButton)

        continueButton.setOnClickListener {
            val age = ageInput.text.toString()
            val weight = weightValue.text.toString()

            if (age.isEmpty()) {
                Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MyMealsActivity::class.java)
                intent.putExtra("AGE", age)
                intent.putExtra("WEIGHT", weight)
                startActivity(intent)
                finish()
            }
        }
    }
}