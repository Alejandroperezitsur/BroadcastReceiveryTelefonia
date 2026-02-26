package com.example.broadcastreceiverytelefona

import android.app.Service
import android.app.PendingIntent
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
        
        Log.d("SmsSenderService", "Iniciando envío de SMS a $phoneNumber: $message")
        
        if (phoneNumber.isNotEmpty() && message.isNotEmpty()) {
            sendSms(phoneNumber, message)
        } else {
            Log.e("SmsSenderService", "Número o mensaje vacío")
        }
        
        return START_NOT_STICKY
    }
    
    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = this.getSystemService(SmsManager::class.java)
            
            // Verificar si el mensaje es muy largo y dividirlo si es necesario
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                // Mensaje largo, enviar en partes
                val sentIntents = ArrayList<PendingIntent>()
                val deliveredIntents = ArrayList<PendingIntent>()
                
                for (i in parts.indices) {
                    sentIntents.add(PendingIntent.getBroadcast(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE))
                    deliveredIntents.add(PendingIntent.getBroadcast(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE))
                }
                
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveredIntents)
                Log.d("SmsSenderService", "SMS multipart enviado a $phoneNumber")
            } else {
                // Mensaje corto, enviar normal
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("SmsSenderService", "SMS simple enviado a $phoneNumber")
            }
            
            // Mostrar notificación de que el SMS fue enviado
            Toast.makeText(this, "SMS automático enviado a $phoneNumber", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("SmsSenderService", "Error al enviar SMS: ${e.message}")
            Toast.makeText(this, "Error al enviar SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
