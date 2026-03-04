package com.example.food

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        createNotificationChannel()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val signInButton = findViewById<MaterialButton>(R.id.signInButton)
        val showMenuButton = findViewById<FloatingActionButton>(R.id.showMenuButton)
        
        signInButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                sendLoginNotification()
                // Navigation according to your requirement (Login -> Assessment)
                val intent = Intent(this, AssessmentActivity::class.java)
                intent.putExtra("USER_EMAIL", email)
                startActivity(intent)
            }
        }

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
                        startActivity(Intent(this, NearbyFoodActivity::class.java))
                        true
                    }
                    R.id.popup_sms -> {
                        startActivity(Intent(this, DietReportActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun sendLoginNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "login_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("App Access")
            .setContentText("You have successfully logged in.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Login Notifications"
            val channel = NotificationChannel("login_channel", name, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("YES") { _, _ -> finishAffinity() }
            .setNegativeButton("NO", null)
            .show()
    }
}