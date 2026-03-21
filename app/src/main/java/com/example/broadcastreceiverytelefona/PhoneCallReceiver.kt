package com.example.broadcastreceiverytelefona

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import org.json.JSONArray

/**
 * BroadcastReceiver que escucha los cambios en el estado del teléfono
 * para detectar llamadas entrantes y activar el envío automático de SMS.
 *
 * Soporta 4 modos de respuesta:
 * - ALL: Responde a todas las llamadas
 * - CONTACTS_ONLY: Solo a contactos guardados
 * - UNKNOWN_ONLY: Solo a números desconocidos
 * - SPECIFIC_NUMBERS: Solo a los números de la lista configurada
 *
 * REGISTRADO EN EL MANIFIESTO como requiere la práctica.
 */
class PhoneCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneCallReceiver"
        private const val PREFS_NAME = "AutoReplyPrefs"
        private const val KEY_ENABLED = "auto_reply_enabled"
        private const val KEY_TARGET_NUMBERS = "target_numbers"
        private const val KEY_MESSAGE = "auto_reply_message"
        private const val KEY_RESPONSE_MODE = "response_mode"
        private const val KEY_REPLY_PRIVATE = "reply_to_private"

        // Modos de respuesta
        const val MODE_ALL = "ALL"
        const val MODE_CONTACTS_ONLY = "CONTACTS_ONLY"
        const val MODE_UNKNOWN_ONLY = "UNKNOWN_ONLY"
        const val MODE_SPECIFIC_NUMBERS = "SPECIFIC_NUMBERS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive llamado con action: ${intent.action}")

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            Log.d(TAG, "Acción no es PHONE_STATE_CHANGED, ignorando")
            return
        }

        if (!hasRequiredPermissions(context)) {
            Log.e(TAG, "No se tienen los permisos necesarios")
            return
        }

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(TAG, "Estado del teléfono: $state")

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            handleIncomingCall(context, intent)
        }
    }

    /**
     * Maneja una llamada entrante verificando si debe responder automáticamente
     * según el modo de respuesta configurado.
     */
    private fun handleIncomingCall(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_ENABLED, false)

        if (!isEnabled) {
            Log.d(TAG, "Respuesta automática deshabilitada por el usuario")
            return
        }

        val autoReplyMessage = prefs.getString(KEY_MESSAGE, "")
        if (autoReplyMessage.isNullOrBlank()) {
            Log.w(TAG, "No hay mensaje configurado")
            return
        }

        val responseMode = prefs.getString(KEY_RESPONSE_MODE, MODE_SPECIFIC_NUMBERS) ?: MODE_SPECIFIC_NUMBERS
        val replyToPrivate = prefs.getBoolean(KEY_REPLY_PRIVATE, false)

        // Obtener número entrante
        val incomingNumber = getIncomingNumber(intent)

        if (incomingNumber.isNullOrBlank()) {
            Log.w(TAG, "No se pudo obtener el número entrante (Llamada privada o restricción de Android 10+).")
            if (replyToPrivate) {
                Log.i(TAG, "Configurado para responder a llamadas privadas, pero sin número no se puede enviar SMS.")
            }
            return
        }

        Log.d(TAG, "Llamada entrante de: $incomingNumber")
        Log.d(TAG, "Modo de respuesta: $responseMode")

        val shouldReply = when (responseMode) {
            MODE_ALL -> {
                Log.d(TAG, "Modo: Responder a TODOS")
                true
            }
            MODE_CONTACTS_ONLY -> {
                val isContact = isContactSaved(context, incomingNumber)
                Log.d(TAG, "Modo: Solo contactos. ¿Es contacto? $isContact")
                isContact
            }
            MODE_UNKNOWN_ONLY -> {
                val isContact = isContactSaved(context, incomingNumber)
                Log.d(TAG, "Modo: Solo desconocidos. ¿Es contacto? $isContact")
                !isContact
            }
            MODE_SPECIFIC_NUMBERS -> {
                val targetNumbers = getTargetNumbers(prefs)
                val matches = targetNumbers.any { numbersMatch(normalizePhoneNumber(incomingNumber), normalizePhoneNumber(it)) }
                Log.d(TAG, "Modo: Números específicos. Lista: $targetNumbers. ¿Coincide? $matches")
                matches
            }
            else -> {
                Log.w(TAG, "Modo desconocido: $responseMode, usando SPECIFIC_NUMBERS")
                val targetNumbers = getTargetNumbers(prefs)
                targetNumbers.any { numbersMatch(normalizePhoneNumber(incomingNumber), normalizePhoneNumber(it)) }
            }
        }

        if (shouldReply) {
            Log.i(TAG, "¡Condición cumplida! Iniciando envío de SMS automático a $incomingNumber")
            startSmsService(context, incomingNumber, autoReplyMessage)
        } else {
            Log.d(TAG, "La llamada no cumple con el filtro configurado. No se enviará SMS.")
        }
    }

    /**
     * Lee la lista de números específicos desde SharedPreferences (almacenados como JSON array).
     */
    private fun getTargetNumbers(prefs: android.content.SharedPreferences): List<String> {
        val json = prefs.getString(KEY_TARGET_NUMBERS, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error al parsear target_numbers: ${e.message}")
            // Fallback: intentar leer el formato antiguo (un solo número)
            val singleNumber = prefs.getString("target_number", "")
            if (!singleNumber.isNullOrBlank()) listOf(singleNumber) else emptyList()
        }
    }

    /**
     * Verifica si un número de teléfono está guardado en los contactos del dispositivo.
     */
    private fun isContactSaved(context: Context, phoneNumber: String): Boolean {
        try {
            if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "No se tiene permiso READ_CONTACTS")
                return false
            }

            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )

            val cursor: Cursor? = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null
            )

            val exists = cursor?.use { it.moveToFirst() } ?: false
            Log.d(TAG, "Número $phoneNumber ${if (exists) "es" else "NO es"} un contacto guardado")
            return exists
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar contacto: ${e.message}")
            return false
        }
    }

    /**
     * Extrae el número de teléfono entrante del intent.
     */
    private fun getIncomingNumber(intent: Intent): String? {
        var number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (number.isNullOrBlank()) {
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
     */
    private fun numbersMatch(number1: String, number2: String): Boolean {
        if (number1 == number2) return true

        val minLength = minOf(number1.length, number2.length)
        if (minLength >= 10) {
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
