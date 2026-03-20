package com.example.broadcastreceiverytelefona

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.broadcastreceiverytelefona.ui.theme.BroadcastReceiverYTelefoníaTheme
import com.example.broadcastreceiverytelefona.ui.theme.SuccessGreen
import com.example.broadcastreceiverytelefona.ui.theme.WarningOrange
import com.example.broadcastreceiverytelefona.ui.theme.GradientStart
import com.example.broadcastreceiverytelefona.ui.theme.GradientEnd
import com.example.broadcastreceiverytelefona.ui.theme.PremiumGold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.os.Build

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { (permission, isGranted) ->
            when {
                isGranted -> { }
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(
                        this,
                        "El permiso $permission es necesario para el funcionamiento de la app",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Permiso $permission denegado permanentemente. Configure en ajustes del sistema.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BroadcastReceiverYTelefoníaTheme {
                AutoReplyApp(
                    onRequestPermissions = { requestPermissions() }
                )
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        val requiredPermissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS
        )

        requiredPermissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReplyApp(onRequestPermissions: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AutoResponder Pro",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            "Intelligent Call Assistant",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onRequestPermissions) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurar permisos",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AutoReplyConfigScreen(
            modifier = Modifier.padding(innerPadding),
            context = context,
            snackbarHostState = snackbarHostState,
            scope = scope
        )
    }
}

@Composable
fun AutoReplyConfigScreen(
    modifier: Modifier = Modifier,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val prefs = remember { context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE) }

    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(false) }
    var hasPhoneError by remember { mutableStateOf(false) }
    var hasMessageError by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var sentMessagesCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        phoneNumber = prefs.getString("target_number", "") ?: ""
        message = prefs.getString("auto_reply_message", "") ?: ""
        isEnabled = prefs.getBoolean("auto_reply_enabled", false)
        sentMessagesCount = prefs.getInt("sent_messages_count", 0)
    }

    val isConfigValid = phoneNumber.isNotBlank() && message.isNotBlank()
    val canEnable = isConfigValid && !hasPhoneError && !hasMessageError

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusCard(
            isEnabled = isEnabled,
            canEnable = canEnable,
            sentMessagesCount = sentMessagesCount,
            onToggle = {
                if (canEnable || isEnabled) {
                    isEnabled = !isEnabled
                    prefs.edit().putBoolean("auto_reply_enabled", isEnabled).apply()

                    val messageText = if (isEnabled) {
                        "Respuesta automática ACTIVADA"
                    } else {
                        "Respuesta automática DESACTIVADA"
                    }

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = messageText,
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Complete la configuración antes de activar",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )

        ConfigurationCard(
            phoneNumber = phoneNumber,
            onPhoneNumberChange = {
                phoneNumber = it
                hasPhoneError = !isValidPhoneNumber(it) && it.isNotBlank()
            },
            hasPhoneError = hasPhoneError,
            message = message,
            onMessageChange = {
                message = it
                hasMessageError = it.length > 160
            },
            hasMessageError = hasMessageError,
            onSave = {
                if (isValidPhoneNumber(phoneNumber) && message.isNotBlank() && message.length <= 160) {
                    prefs.edit().apply {
                        putString("target_number", phoneNumber)
                        putString("auto_reply_message", message)
                        apply()
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Configuración guardada exitosamente",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Por favor, verifique los campos",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            onTestSms = {
                if (phoneNumber.isNotBlank() && message.isNotBlank()) {
                    val serviceIntent = Intent(context, SmsSenderService::class.java).apply {
                        putExtra(SmsSenderService.EXTRA_PHONE_NUMBER, phoneNumber)
                        putExtra(SmsSenderService.EXTRA_MESSAGE, message)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Iniciando envío de SMS de prueba...")
                    }
                }
            },
            isEnabled = isEnabled
        )

        OutlinedButton(
            onClick = { showHistory = !showHistory },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showHistory) "Ocultar historial" else "Ver historial de mensajes")
        }

        AnimatedVisibility(
            visible = showHistory,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            HistoryCard(context = context)
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoSection()
    }
}

@Composable
fun StatusCard(
    isEnabled: Boolean,
    canEnable: Boolean,
    sentMessagesCount: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isEnabled) {
                        Brush.verticalGradient(listOf(SuccessGreen, SuccessGreen.copy(alpha = 0.7f)))
                    } else {
                        Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                    }
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) SuccessGreen.copy(alpha = 0.2f)
                        else WarningOrange.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isEnabled) SuccessGreen else WarningOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isEnabled) "Sistema Activo" else "Sistema Inactivo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnabled) {
                    "La app responderá automáticamente con SMS"
                } else {
                    "Active el sistema para comenzar a responder automáticamente"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (sentMessagesCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$sentMessagesCount mensajes enviados",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggle,
                enabled = canEnable || isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEnabled) "DESACTIVAR" else "ACTIVAR",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            } // end Column
        } // end Box
    } // end Card
}

@Composable
fun ConfigurationCard(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    hasPhoneError: Boolean,
    message: String,
    onMessageChange: (String) -> Unit,
    hasMessageError: Boolean,
    onSave: () -> Unit,
    onTestSms: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider()

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Número telefónico a responder") },
                placeholder = { Text("Ej: +52 123 456 7890") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = hasPhoneError,
                supportingText = {
                    if (hasPhoneError) {
                        Text("Ingrese un número válido", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Incluya código de país (opcional)")
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Mensaje de respuesta automática") },
                placeholder = { Text("Ej: No puedo atender ahora, te contacto más tarde.") },
                modifier = Modifier.fillMaxWidth(),
                isError = hasMessageError,
                supportingText = {
                    val remaining = 160 - message.length
                    Text(
                        text = "$remaining caracteres restantes",
                        color = if (remaining < 0) MaterialTheme.colorScheme.error
                        else if (remaining < 20) WarningOrange
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onTestSms,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = phoneNumber.isNotBlank() && message.isNotBlank(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GradientStart)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Probar SMS", fontSize = 14.sp)
                }
            }

            if (isEnabled) {
                Text(
                    text = "Desactive el sistema para modificar la configuración",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun HistoryCard(context: Context) {
    val prefs = remember { context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE) }
    val historyJson = prefs.getString("message_history", "") ?: ""

    val history = remember(historyJson) {
        if (historyJson.isNotEmpty()) {
            historyJson.split("|||").mapNotNull { entry ->
                val parts = entry.split("||")
                if (parts.size >= 2) {
                    HistoryEntry(parts[0], parts[1])
                } else null
            }.reversed()
        } else emptyList()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Historial de Mensajes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay mensajes enviados aún",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                history.take(5).forEachIndexed { index, entry ->
                    HistoryItem(entry)
                    if (index < minOf(history.size, 5) - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = SuccessGreen
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = entry.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoItem(
                icon = Icons.Default.Info,
                text = "La app detecta llamadas entrantes del número configurado y responde automáticamente con el mensaje establecido."
            )
            InfoItem(
                icon = Icons.Default.Home,
                text = "Se requieren permisos de teléfono y SMS para el funcionamiento correcto."
            )
            InfoItem(
                icon = Icons.Default.Notifications,
                text = "Recibirás una notificación cada vez que se envíe un SMS automático."
            )
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class HistoryEntry(val phoneNumber: String, val timestamp: String)

fun isValidPhoneNumber(number: String): Boolean {
    if (number.isBlank()) return false
    val cleaned = number.replace("[^0-9+]".toRegex(), "")
    return cleaned.length >= 10 && cleaned.length <= 15
}
