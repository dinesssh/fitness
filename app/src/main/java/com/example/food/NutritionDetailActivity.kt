package com.example.food

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
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
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

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
        val btnCapturePhoto = findViewById<MaterialButton>(R.id.btnCapturePhoto)
        ivMealPreview = findViewById(R.id.ivMealPreview)

        // Setup Meal Type Dropdown
        val mealTypes = arrayOf("Breakfast", "Lunch", "Dinner", "Snacks")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mealTypes)
        mealTypeDropdown.setAdapter(adapter)
        
        // Auto-select based on intent or default
        val intentType = intent.getStringExtra("MEAL_TYPE") ?: "Breakfast"
        mealTypeDropdown.setText(intentType, false)

        btnBack.setOnClickListener { finish() }

        btnCapturePhoto.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

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
                    val imageBytes = capturedImage?.let { bitmapToByteArray(it) }
                    
                    db.addMeal(
                        name,
                        calStr.toInt(),
                        proStr.toDouble(),
                        carbStr.toDouble(),
                        fatStr.toDouble(),
                        fiberStr.toDouble(),
                        type,
                        imageBytes
                    )
                    Toast.makeText(this, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving meal. Check numeric inputs.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            cameraLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
}