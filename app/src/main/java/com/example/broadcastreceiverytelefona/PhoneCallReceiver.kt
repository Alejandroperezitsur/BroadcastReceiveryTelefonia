package com.example.broadcastreceiverytelefona

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class PhoneCallReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            Log.d("PhoneCallReceiver", "Estado: $state, Número: $incomingNumber")
            
            if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
                // Hay una llamada entrante
                handleIncomingCall(context, incomingNumber)
            }
        }
    }
    
    private fun handleIncomingCall(context: Context, phoneNumber: String) {
        // Obtener el número configurado desde SharedPreferences
        val prefs = context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE)
        val targetNumber = prefs.getString("target_number", "")
        val autoReplyMessage = prefs.getString("auto_reply_message", "")
        
        Log.d("PhoneCallReceiver", "Número objetivo: $targetNumber, Mensaje: $autoReplyMessage")
        
        // Verificar si el número entrante coincide con el configurado
        if (phoneNumber == targetNumber && !autoReplyMessage.isNullOrEmpty()) {
            // Iniciar el servicio para enviar SMS
            val serviceIntent = Intent(context, SmsSenderService::class.java).apply {
                putExtra("phone_number", phoneNumber)
                putExtra("message", autoReplyMessage)
            }
            context.startService(serviceIntent)
        }
    }
}
