package com.example.broadcastreceiverytelefona

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Servicio que envía SMS automáticamente cuando se activa desde el BroadcastReceiver.
 * Ejecuta el envío en primer plano para asegurar que se complete incluso si la app
 * está en segundo plano.
 */
class SmsSenderService : Service() {

    companion object {
        private const val TAG = "SmsSenderService"
        private const val CHANNEL_ID = "AutoReplyChannel"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "AutoReplyPrefs"

        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_MESSAGE = "message"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand llamado")

        val phoneNumber = intent?.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: ""

        if (phoneNumber.isBlank() || message.isBlank()) {
            Log.e(TAG, "Número o mensaje vacío, deteniendo servicio")
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createForegroundNotification(phoneNumber)
        startForeground(NOTIFICATION_ID, notification)

        Thread {
            try {
                sendSms(phoneNumber, message)
                insertSmsIntoSentFolder(phoneNumber, message)
                saveToHistory(this, phoneNumber, message)
                showSuccessNotification(phoneNumber)
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar el envío de SMS: ${e.message}", e)
                showErrorNotification(phoneNumber, e.message ?: "Error desconocido")
            } finally {
                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendSms(phoneNumber: String, message: String) {
        Log.d(TAG, "Enviando SMS a: $phoneNumber")
        Log.d(TAG, "Mensaje: $message")

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)

            if (parts.size > 1) {
                Log.d(TAG, "Enviando mensaje multipart (${parts.size} partes)")

                val sentIntents = ArrayList<PendingIntent>()
                val deliveredIntents = ArrayList<PendingIntent>()

                parts.indices.forEach { _ ->
                    sentIntents.add(PendingIntent.getBroadcast(
                        this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE
                    ))
                    deliveredIntents.add(PendingIntent.getBroadcast(
                        this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE
                    ))
                }

                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    sentIntents,
                    deliveredIntents
                )
            } else {
                Log.d(TAG, "Enviando mensaje simple")
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }

            Log.i(TAG, "SMS enviado exitosamente a: $phoneNumber")

        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso SEND_SMS no concedido: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando SMS: ${e.message}")
            throw e
        }
    }

    /**
     * Inserta el mensaje enviado en la base de datos de SMS del sistema
     * para que aparezca en la aplicación de mensajes predeterminada.
     */
    private fun insertSmsIntoSentFolder(phoneNumber: String, message: String) {
        try {
            val values = ContentValues().apply {
                put("address", phoneNumber)
                put("body", message)
                put("date", System.currentTimeMillis())
                put("read", 1)
                put("type", 2) // 2 es MESSAGE_TYPE_SENT
            }

            contentResolver.insert(Uri.parse("content://sms/sent"), values)
            Log.d(TAG, "SMS insertado en la carpeta 'Sent' del sistema")
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo insertar en el historial del sistema: ${e.message}")
        }
    }

    /**
     * Guarda el mensaje en el historial interno de la app.
     * Formato: phoneNumber||timestamp||message|||nextEntry...
     */
    private fun saveToHistory(context: Context, phoneNumber: String, message: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentHistory = prefs.getString("message_history", "") ?: ""

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        // Escapar el mensaje para evitar conflictos con los delimitadores
        val safeMessage = message.replace("|||", "| | |").replace("||", "| |")

        val newEntry = "$phoneNumber||$timestamp||$safeMessage"
        val updatedHistory = if (currentHistory.isEmpty()) {
            newEntry
        } else {
            "$newEntry|||$currentHistory"
        }

        val entries = updatedHistory.split("|||")
        val limitedHistory = if (entries.size > 50) {
            entries.take(50).joinToString("|||")
        } else updatedHistory

        prefs.edit().apply {
            putString("message_history", limitedHistory)
            putInt("sent_messages_count", prefs.getInt("sent_messages_count", 0) + 1)
            apply()
        }

        Log.d(TAG, "Mensaje guardado en historial con texto")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Respuesta Automática",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del servicio de respuesta automática"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado")
        }
    }

    private fun createForegroundNotification(phoneNumber: String): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enviando respuesta automática...")
            .setContentText("A: $phoneNumber")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun showSuccessNotification(phoneNumber: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Respuesta automática enviada")
            .setContentText("Se envió SMS a $phoneNumber")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun showErrorNotification(phoneNumber: String, error: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Error al enviar respuesta")
            .setContentText("No se pudo enviar SMS a $phoneNumber: $error")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
}
