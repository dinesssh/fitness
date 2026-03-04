package com.example.food

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class SmsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSendSms = findViewById<Button>(R.id.btnSendSms)

        btnSendSms.setOnClickListener {
            val phone = etPhoneNumber.text.toString()
            val message = etMessage.text.toString()

            if (phone.isNotEmpty() && message.isNotEmpty()) {
                checkSmsPermission(phone, message)
            } else {
                Toast.makeText(this, "Please enter phone and message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSmsPermission(phone: String, message: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        } else {
            sendSms(phone, message)
        }
    }

    private fun sendSms(phone: String, message: String) {
        try {
            val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                this.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phone, null, message, null, null)
            Toast.makeText(this, "SMS Sent successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "SMS Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val phone = findViewById<EditText>(R.id.etPhoneNumber).text.toString()
            val message = findViewById<EditText>(R.id.etMessage).text.toString()
            sendSms(phone, message)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}