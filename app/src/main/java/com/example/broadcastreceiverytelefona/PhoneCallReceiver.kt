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
        var targetNumber = prefs.getString("target_number", "")
        val autoReplyMessage = prefs.getString("auto_reply_message", "")
        
        Log.d("PhoneCallReceiver", "Número entrante: $phoneNumber, Número objetivo: $targetNumber, Mensaje: $autoReplyMessage")
        
        // Normalizar números para comparación (quitar espacios, guiones, paréntesis)
        val normalizedIncoming = normalizePhoneNumber(phoneNumber)
        val normalizedTarget = normalizePhoneNumber(targetNumber ?: "")
        
        Log.d("PhoneCallReceiver", "Normalizado - Entrante: $normalizedIncoming, Objetivo: $normalizedTarget")
        
        // Verificar si el número entrante coincide con el configurado
        if (normalizedIncoming == normalizedTarget && !autoReplyMessage.isNullOrEmpty()) {
            // Iniciar el servicio para enviar SMS
            val serviceIntent = Intent(context, SmsSenderService::class.java).apply {
                putExtra("phone_number", phoneNumber)
                putExtra("message", autoReplyMessage)
            }
            context.startService(serviceIntent)
            Log.d("PhoneCallReceiver", "Servicio de SMS iniciado")
        } else {
            Log.d("PhoneCallReceiver", "No coincide el número o no hay mensaje")
        }
    }
    
    private fun normalizePhoneNumber(number: String): String {
        return number.replace("[^0-9+]".toRegex(), "")
    }
}
