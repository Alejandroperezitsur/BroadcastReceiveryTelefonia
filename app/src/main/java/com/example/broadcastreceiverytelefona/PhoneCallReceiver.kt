package com.example.broadcastreceiverytelefona

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

/**
 * BroadcastReceiver que escucha los cambios en el estado del teléfono
 * para detectar llamadas entrantes y activar el envío automático de SMS.
 *
 * REGISTRADO EN EL MANIFIESTO como requiere la práctica.
 */
class PhoneCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneCallReceiver"
        private const val PREFS_NAME = "AutoReplyPrefs"
        private const val KEY_ENABLED = "auto_reply_enabled"
        private const val KEY_TARGET_NUMBER = "target_number"
        private const val KEY_MESSAGE = "auto_reply_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive llamado con action: ${intent.action}")

        // Verificar que la acción sea la correcta
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            Log.d(TAG, "Acción no es PHONE_STATE_CHANGED, ignorando")
            return
        }

        // Verificar permisos antes de procesar
        if (!hasRequiredPermissions(context)) {
            Log.e(TAG, "No se tienen los permisos necesarios")
            return
        }

        // Obtener el estado del teléfono
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(TAG, "Estado del teléfono: $state")

        // Solo procesar cuando está sonando (llamada entrante)
        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            handleIncomingCall(context, intent)
        }
    }

    /**
     * Maneja una llamada entrante verificando si debe responder automáticamente
     */
    private fun handleIncomingCall(context: Context, intent: Intent) {
        // Obtener número entrante
        val incomingNumber = getIncomingNumber(intent)

        if (incomingNumber.isNullOrBlank()) {
            Log.w(TAG, "No se pudo obtener el número entrante (Llamada privada o restricción de Android 10+).")
            return
        }

        Log.d(TAG, "Llamada entrante de: $incomingNumber")

        // Verificar si la respuesta automática está habilitada
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_ENABLED, false)

        if (!isEnabled) {
            Log.d(TAG, "Respuesta automática deshabilitada por el usuario")
            return
        }

        val targetNumber = prefs.getString(KEY_TARGET_NUMBER, "")
        val autoReplyMessage = prefs.getString(KEY_MESSAGE, "")

        Log.d(TAG, "Configuración - Número objetivo: $targetNumber, Mensaje: $autoReplyMessage")

        // Validar configuración
        if (targetNumber.isNullOrBlank() || autoReplyMessage.isNullOrBlank()) {
            Log.w(TAG, "Configuración incompleta")
            return
        }

        // Normalizar números para comparación
        val normalizedIncoming = normalizePhoneNumber(incomingNumber)
        val normalizedTarget = normalizePhoneNumber(targetNumber)

        Log.d(TAG, "Comparando - Entrante: $normalizedIncoming, Objetivo: $normalizedTarget")

        // Verificar coincidencia
        if (numbersMatch(normalizedIncoming, normalizedTarget)) {
            Log.i(TAG, "¡Número coincide! Iniciando envío de SMS automático")
            startSmsService(context, incomingNumber, autoReplyMessage)
        } else {
            Log.d(TAG, "El número entrante no coincide con el configurado")
        }
    }

    /**
     * Extrae el número de teléfono entrante del intent.
     * En Android 10+ (API 29+), se requiere READ_CALL_LOG para obtener el número.
     */
    private fun getIncomingNumber(intent: Intent): String? {
        var number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        // Si no se obtiene el número directamente, intentar obtenerlo de otras formas
        // Esto puede variar según la versión de Android y el fabricante
        if (number.isNullOrBlank()) {
            // En algunos dispositivos, el número puede venir en otros extras
            number = intent.getStringExtra("incoming_number")
        }

        return number
    }

    /**
     * Verifica que la app tenga los permisos necesarios
     */
    private fun hasRequiredPermissions(context: Context): Boolean {
        val requiredPermissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.SEND_SMS
        )

        return requiredPermissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Normaliza un número de teléfono para comparación
     */
    private fun normalizePhoneNumber(number: String): String {
        return number.replace("[^0-9]".toRegex(), "")
    }

    /**
     * Compara dos números de teléfono normalizados
     * Permite coincidencia parcial (últimos dígitos) para mayor flexibilidad
     */
    private fun numbersMatch(number1: String, number2: String): Boolean {
        if (number1 == number2) return true

        // Si uno contiene al otro (para manejar prefijos de país)
        val minLength = minOf(number1.length, number2.length)
        if (minLength >= 10) {
            // Comparar los últimos 10 dígitos (número sin prefijo de país)
            val end1 = number1.takeLast(10)
            val end2 = number2.takeLast(10)
            return end1 == end2
        }

        return false
    }

    /**
     * Inicia el servicio para enviar el SMS
     */
    private fun startSmsService(context: Context, phoneNumber: String, message: String) {
        try {
            val serviceIntent = Intent(context, SmsSenderService::class.java).apply {
                putExtra(SmsSenderService.EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(SmsSenderService.EXTRA_MESSAGE, message)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.i(TAG, "Servicio SmsSenderService iniciado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar el servicio de SMS: ${e.message}", e)
        }
    }
}
