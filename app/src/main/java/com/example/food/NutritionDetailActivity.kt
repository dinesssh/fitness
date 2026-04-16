package com.example.food

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

class NutritionDetailActivity : AppCompatActivity() {

    private var capturedImage: Bitmap? = null
    private lateinit var ivMealPreview: ImageView
    
    // Local food database (per 100g)
    private val foodDatabase = mapOf(
        "Rice" to Nutrition(365, 7.1, 80.0, 0.7, 1.3),
        "Chicken Breast" to Nutrition(165, 31.0, 0.0, 3.6, 0.0),
        "Egg" to Nutrition(155, 13.0, 1.1, 11.0, 0.0),
        "Apple" to Nutrition(52, 0.3, 14.0, 0.2, 2.4),
        "Banana" to Nutrition(89, 1.1, 23.0, 0.3, 2.6),
        "Oats" to Nutrition(389, 16.9, 66.0, 6.9, 10.6),
        "Milk" to Nutrition(42, 3.4, 5.0, 1.0, 0.0)
    )

    data class Nutrition(val cal: Int, val pro: Double, val carb: Double, val fat: Double, val fib: Double)

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                capturedImage = imageBitmap
                ivMealPreview.setImageBitmap(imageBitmap)
                ivMealPreview.visibility = View.VISIBLE
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera()
        else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_detail)

        val db = DatabaseHelper(this)
        
        val mealTypeDropdown = findViewById<AutoCompleteTextView>(R.id.mealTypeDropdown)
        val etName = findViewById<AutoCompleteTextView>(R.id.etMealName)
        val etQuantity = findViewById<TextInputEditText>(R.id.etQuantity)
        val etCal = findViewById<TextInputEditText>(R.id.etCalories)
        val etPro = findViewById<TextInputEditText>(R.id.etProtein)
        val etCarb = findViewById<TextInputEditText>(R.id.etCarbs)
        val etFat = findViewById<TextInputEditText>(R.id.etFat)
        val etFiber = findViewById<TextInputEditText>(R.id.etFiber)
        val btnSave = findViewById<MaterialButton>(R.id.addMealButton)
        val btnBack = findViewById<ImageView>(R.id.backButton)
        val btnCapturePhoto = findViewById<MaterialButton>(R.id.btnCapturePhoto)
        ivMealPreview = findViewById(R.id.ivMealPreview)

        // Setup Food Autocomplete
        val foodAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, foodDatabase.keys.toList())
        etName.setAdapter(foodAdapter)

        // Setup Meal Type Dropdown
        val mealTypes = arrayOf("Breakfast", "Lunch", "Dinner", "Snacks")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mealTypes)
        mealTypeDropdown.setAdapter(typeAdapter)
        
        val intentType = intent.getStringExtra("MEAL_TYPE") ?: "Breakfast"
        mealTypeDropdown.setText(intentType, false)

        // Live Calculation Listeners
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { calculateNutrition() }
        }
        etName.addTextChangedListener(textWatcher)
        etQuantity.addTextChangedListener(textWatcher)

        btnBack.setOnClickListener { finish() }
        btnCapturePhoto.setOnClickListener { checkCameraPermissionAndOpen() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            
            if (name.isEmpty() || quantity <= 0) {
                Toast.makeText(this, "Enter food name and quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val imageBytes = capturedImage?.let { bitmapToByteArray(it) }
                db.addMeal(
                    name,
                    etCal.text.toString().toInt(),
                    etPro.text.toString().toDouble(),
                    etCarb.text.toString().toDouble(),
                    etFat.text.toString().toDouble(),
                    etFiber.text.toString().toDouble(),
                    mealTypeDropdown.text.toString(),
                    imageBytes
                )
                Toast.makeText(this, "Meal added successfully 🍽️", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateNutrition() {
        val etName = findViewById<AutoCompleteTextView>(R.id.etMealName)
        val etQuantity = findViewById<TextInputEditText>(R.id.etQuantity)
        val foodName = etName.text.toString()
        val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
        
        val nutrition = foodDatabase[foodName]
        if (nutrition != null && quantity > 0) {
            val factor = quantity / 100.0
            findViewById<TextInputEditText>(R.id.etCalories).setText((nutrition.cal * factor).toInt().toString())
            findViewById<TextInputEditText>(R.id.etProtein).setText(String.format("%.1f", nutrition.pro * factor))
            findViewById<TextInputEditText>(R.id.etCarbs).setText(String.format("%.1f", nutrition.carb * factor))
            findViewById<TextInputEditText>(R.id.etFat).setText(String.format("%.1f", nutrition.fat * factor))
            findViewById<TextInputEditText>(R.id.etFiber).setText(String.format("%.1f", nutrition.fib * factor))
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openCamera()
        else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try { cameraLauncher.launch(intent) } catch (e: Exception) {}
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
}