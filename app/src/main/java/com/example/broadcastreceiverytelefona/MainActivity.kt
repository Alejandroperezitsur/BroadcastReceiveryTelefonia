package com.example.broadcastreceiverytelefona

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.broadcastreceiverytelefona.ui.theme.BroadcastReceiverYTelefoníaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BroadcastReceiverYTelefoníaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AutoReplyConfigScreen(
                        modifier = Modifier.padding(innerPadding),
                        context = this@MainActivity
                    )
                }
            }
        }
    }
}

@Composable
fun AutoReplyConfigScreen(modifier: Modifier = Modifier, context: Context) {
    var phoneNumber by remember { mutableStateOf(TextFieldValue()) }
    var message by remember { mutableStateOf(TextFieldValue()) }
    
    // Cargar configuración guardada
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE)
        phoneNumber = TextFieldValue(prefs.getString("target_number", "") ?: "")
        message = TextFieldValue(prefs.getString("auto_reply_message", "") ?: "")
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Configuración de Respuesta Automática",
            style = MaterialTheme.typography.headlineMedium
        )
        
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número telefónico") },
            placeholder = { Text("Ej: 1234567890") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensaje de respuesta") },
            placeholder = { Text("Ej: No puedo atender ahora, te escribo luego.") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        
        Button(
            onClick = {
                saveConfiguration(context, phoneNumber.text, message.text)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Configuración")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "La aplicación responderá automáticamente con un SMS cuando reciba una llamada del número configurado.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private fun saveConfiguration(context: Context, phoneNumber: String, message: String) {
    val prefs = context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putString("target_number", phoneNumber)
        putString("auto_reply_message", message)
        apply()
    }
}