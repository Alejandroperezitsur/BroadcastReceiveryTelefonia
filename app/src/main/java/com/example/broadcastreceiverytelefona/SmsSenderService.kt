package com.example.broadcastreceiverytelefona

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

class SmsSenderService : Service() {
    
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phone_number") ?: ""
        val message = intent?.getStringExtra("message") ?: ""
        
        Log.d("SmsSenderService", "Enviando SMS a $phoneNumber: $message")
        
        if (phoneNumber.isNotEmpty() && message.isNotEmpty()) {
            sendSms(phoneNumber, message)
        }
        
        return START_NOT_STICKY
    }
    
    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = this.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            
            Log.d("SmsSenderService", "SMS enviado exitosamente a $phoneNumber")
            
            // Mostrar notificación de que el SMS fue enviado
            Toast.makeText(this, "SMS automático enviado a $phoneNumber", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("SmsSenderService", "Error al enviar SMS: ${e.message}")
            Toast.makeText(this, "Error al enviar SMS automático", Toast.LENGTH_SHORT).show()
        }
    }
}
