package com.example.food

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Start Foreground Service
        val serviceIntent = Intent(this, MealTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val showMenuButton = findViewById<MaterialButton>(R.id.showMenuButton)

        showMenuButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.popup_about -> {
                        startActivity(Intent(this, AboutUsActivity::class.java))
                        true
                    }
                    R.id.popup_team_details -> {
                        startActivity(Intent(this, TeamDetailsActivity::class.java))
                        true
                    }
                    R.id.popup_project_description -> {
                        startActivity(Intent(this, ProjectDescriptionActivity::class.java))
                        true
                    }
                    R.id.popup_location -> {
                        // Corrected activity name to match AndroidManifest
                        startActivity(Intent(this, NearbyFoodActivity::class.java))
                        true
                    }
                    R.id.popup_sms -> {
                        // Corrected activity name to match AndroidManifest
                        startActivity(Intent(this, DietReportActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}