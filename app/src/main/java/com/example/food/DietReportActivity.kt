package com.example.food

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton

class DietReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_report)

        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val tvReportContent = findViewById<TextView>(R.id.tvReportContent)
        val btnSendSms = findViewById<MaterialButton>(R.id.btnSendSms)

        btnSendSms.setOnClickListener {
            val phone = etPhoneNumber.text.toString().trim()
            val message = tvReportContent.text.toString()

            if (phone.isNotEmpty()) {
                checkSmsPermission(phone, message)
            } else {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSmsPermission(phone: String, message: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 102)
        } else {
            sendSms(phone, message)
        }
    }

    private fun sendSms(phone: String, message: String) {
        try {
            // Simplified SMS manager for broader compatibility
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            smsManager.sendTextMessage(phone, null, message, null, null)
            Toast.makeText(this, "Diet report sent successfully to $phone", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Detailed error for debugging
            val errorMsg = when {
                e.message?.contains("service", ignoreCase = true) == true -> "No SIM service"
                e.message?.contains("permission", ignoreCase = true) == true -> "Permission denied"
                else -> "SMS Failed: ${e.message}"
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val phone = findViewById<EditText>(R.id.etPhoneNumber).text.toString().trim()
            val message = findViewById<TextView>(R.id.tvReportContent).text.toString()
            if (phone.isNotEmpty()) sendSms(phone, message)
        } else {
            Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}