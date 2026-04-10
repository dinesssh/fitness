package com.example.food

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        createNotificationChannel()

        val loginCard = findViewById<MaterialCardView>(R.id.loginCard)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val signInButton = findViewById<MaterialButton>(R.id.signInButton)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val signUpText = findViewById<TextView>(R.id.signUpText)

        // Fade-in animation for the login card
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000
        fadeIn.fillAfter = true
        loginCard.startAnimation(fadeIn)
        loginCard.alpha = 1f

        signInButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                // Show loading indicator
                signInButton.visibility = View.GONE
                loadingIndicator.visibility = View.VISIBLE

                // Simulate login delay
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    sendLoginNotification()
                    
                    // Navigate to Assessment
                    val intent = Intent(this, AssessmentActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                    finish()
                }, 1500)
            }
        }

        forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        signUpText.setOnClickListener {
            Toast.makeText(this, "Sign up clicked", Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun sendLoginNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "login_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("App Access")
            .setContentText("You have successfully logged in to Fitness Tracker.")
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
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("YES") { _, _ -> finishAffinity() }
            .setNegativeButton("NO", null)
            .show()
    }
}