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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.broadcastreceiverytelefona.ui.theme.BroadcastReceiverYTelefoníaTheme
import com.example.broadcastreceiverytelefona.ui.theme.SuccessGreen
import com.example.broadcastreceiverytelefona.ui.theme.WarningOrange
import com.example.broadcastreceiverytelefona.ui.theme.GradientStart
import com.example.broadcastreceiverytelefona.ui.theme.GradientEnd
import com.example.broadcastreceiverytelefona.ui.theme.PremiumGold
import com.example.broadcastreceiverytelefona.ui.theme.InfoBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.os.Build
import org.json.JSONArray

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
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
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

// --- Screens enum ---
enum class Screen {
    HOME, SETTINGS
}

// --- Response Mode constants ---
object ResponseMode {
    const val ALL = "ALL"
    const val CONTACTS_ONLY = "CONTACTS_ONLY"
    const val UNKNOWN_ONLY = "UNKNOWN_ONLY"
    const val SPECIFIC_NUMBERS = "SPECIFIC_NUMBERS"
}

// --- Main App Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReplyApp(onRequestPermissions: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

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
                            if (currentScreen == Screen.HOME) "Intelligent Call Assistant"
                            else "Configuración",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    if (currentScreen == Screen.SETTINGS) {
                        IconButton(onClick = { currentScreen = Screen.HOME }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (currentScreen == Screen.HOME) {
                        IconButton(onClick = { currentScreen = Screen.SETTINGS }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> AutoReplyConfigScreen(
                modifier = Modifier.padding(innerPadding),
                context = context,
                snackbarHostState = snackbarHostState,
                scope = scope
            )
            Screen.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                context = context,
                snackbarHostState = snackbarHostState,
                scope = scope,
                onRequestPermissions = onRequestPermissions
            )
        }
    }
}

// --- Helper functions for multi-number JSON list ---
fun loadNumbersList(prefs: android.content.SharedPreferences): List<String> {
    val json = prefs.getString("target_numbers", "[]") ?: "[]"
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) {
        // Migrate from old single number format
        val old = prefs.getString("target_number", "")
        if (!old.isNullOrBlank()) listOf(old) else emptyList()
    }
}

fun saveNumbersList(prefs: android.content.SharedPreferences, numbers: List<String>) {
    val arr = JSONArray()
    numbers.forEach { arr.put(it) }
    prefs.edit().putString("target_numbers", arr.toString()).apply()
}

// --- Main Config Screen ---
@Composable
fun AutoReplyConfigScreen(
    modifier: Modifier = Modifier,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val prefs = remember { context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE) }

    var message by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(false) }
    var hasMessageError by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var sentMessagesCount by remember { mutableIntStateOf(0) }
    var responseMode by remember { mutableStateOf(ResponseMode.SPECIFIC_NUMBERS) }
    var numbersList by remember { mutableStateOf(listOf<String>()) }
    var newPhoneNumber by remember { mutableStateOf("") }
    var hasPhoneError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        message = prefs.getString("auto_reply_message", "") ?: ""
        isEnabled = prefs.getBoolean("auto_reply_enabled", false)
        sentMessagesCount = prefs.getInt("sent_messages_count", 0)
        responseMode = prefs.getString("response_mode", ResponseMode.SPECIFIC_NUMBERS) ?: ResponseMode.SPECIFIC_NUMBERS
        numbersList = loadNumbersList(prefs)
    }

    val canEnable = message.isNotBlank() && !hasMessageError &&
            (responseMode != ResponseMode.SPECIFIC_NUMBERS || numbersList.isNotEmpty())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
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

        // Response Mode Card
        ResponseModeCard(
            selectedMode = responseMode,
            isEnabled = isEnabled,
            onModeSelected = { mode ->
                responseMode = mode
                prefs.edit().putString("response_mode", mode).apply()
            }
        )

        // Phone Numbers Card (only visible in SPECIFIC_NUMBERS mode)
        AnimatedVisibility(
            visible = responseMode == ResponseMode.SPECIFIC_NUMBERS,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            PhoneNumbersCard(
                numbersList = numbersList,
                newPhoneNumber = newPhoneNumber,
                onNewPhoneNumberChange = {
                    newPhoneNumber = it
                    hasPhoneError = !isValidPhoneNumber(it) && it.isNotBlank()
                },
                hasPhoneError = hasPhoneError,
                isEnabled = isEnabled,
                onAddNumber = {
                    if (isValidPhoneNumber(newPhoneNumber) && !numbersList.contains(newPhoneNumber)) {
                        numbersList = numbersList + newPhoneNumber
                        saveNumbersList(prefs, numbersList)
                        newPhoneNumber = ""
                        scope.launch {
                            snackbarHostState.showSnackbar("Número agregado")
                        }
                    }
                },
                onRemoveNumber = { number ->
                    numbersList = numbersList - number
                    saveNumbersList(prefs, numbersList)
                }
            )
        }

        // Message Configuration Card
        MessageCard(
            message = message,
            onMessageChange = {
                message = it
                hasMessageError = it.length > 160
            },
            hasMessageError = hasMessageError,
            isEnabled = isEnabled,
            onSave = {
                if (message.isNotBlank() && message.length <= 160) {
                    prefs.edit().putString("auto_reply_message", message).apply()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Mensaje guardado exitosamente",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Verifique el mensaje",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            onTestSms = {
                if (message.isNotBlank()) {
                    val numbersToSend = if (responseMode == ResponseMode.SPECIFIC_NUMBERS) {
                        numbersList
                    } else {
                        // For test in non-specific mode, ask for a number
                        if (newPhoneNumber.isNotBlank() && isValidPhoneNumber(newPhoneNumber)) {
                            listOf(newPhoneNumber)
                        } else {
                            numbersList
                        }
                    }

                    if (numbersToSend.isNotEmpty()) {
                        numbersToSend.forEach { number ->
                            val serviceIntent = Intent(context, SmsSenderService::class.java).apply {
                                putExtra(SmsSenderService.EXTRA_PHONE_NUMBER, number)
                                putExtra(SmsSenderService.EXTRA_MESSAGE, message)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("Enviando SMS de prueba a ${numbersToSend.size} número(s)...")
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Agregue al menos un número para probar")
                        }
                    }
                }
            }
        )

        // History Button
        OutlinedButton(
            onClick = { showHistory = !showHistory },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
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

// --- Status Card ---
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
            }
        }
    }
}

// --- Response Mode Card ---
@Composable
fun ResponseModeCard(
    selectedMode: String,
    isEnabled: Boolean,
    onModeSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¿A quién responder?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider()

            ResponseModeOption(
                icon = Icons.Default.Home,
                title = "Todas las llamadas",
                description = "Responde a cualquier número que llame",
                isSelected = selectedMode == ResponseMode.ALL,
                enabled = !isEnabled,
                onClick = { onModeSelected(ResponseMode.ALL) }
            )

            ResponseModeOption(
                icon = Icons.Default.Person,
                title = "Solo contactos",
                description = "Solo responde a números guardados en tu dispositivo",
                isSelected = selectedMode == ResponseMode.CONTACTS_ONLY,
                enabled = !isEnabled,
                onClick = { onModeSelected(ResponseMode.CONTACTS_ONLY) }
            )

            ResponseModeOption(
                icon = Icons.Default.Warning,
                title = "Solo desconocidos",
                description = "Responde a números que NO están en tus contactos",
                isSelected = selectedMode == ResponseMode.UNKNOWN_ONLY,
                enabled = !isEnabled,
                onClick = { onModeSelected(ResponseMode.UNKNOWN_ONLY) }
            )

            ResponseModeOption(
                icon = Icons.Default.Phone,
                title = "Números específicos",
                description = "Solo responde a los números de tu lista personalizada",
                isSelected = selectedMode == ResponseMode.SPECIFIC_NUMBERS,
                enabled = !isEnabled,
                onClick = { onModeSelected(ResponseMode.SPECIFIC_NUMBERS) }
            )

            if (isEnabled) {
                Text(
                    text = "Desactive el sistema para cambiar el modo de respuesta",
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
fun ResponseModeOption(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "modeBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "modeBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// --- Phone Numbers Card (chips) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneNumbersCard(
    numbersList: List<String>,
    newPhoneNumber: String,
    onNewPhoneNumberChange: (String) -> Unit,
    hasPhoneError: Boolean,
    isEnabled: Boolean,
    onAddNumber: () -> Unit,
    onRemoveNumber: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Números Específicos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider()

            // Chips/tags for existing numbers
            if (numbersList.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    numbersList.forEach { number ->
                        PhoneChip(
                            number = number,
                            onRemove = { onRemoveNumber(number) },
                            enabled = !isEnabled
                        )
                    }
                }
            } else {
                Text(
                    text = "No hay números agregados. Agregue al menos uno.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Add new number
            if (!isEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newPhoneNumber,
                        onValueChange = onNewPhoneNumberChange,
                        label = { Text("Agregar número") },
                        placeholder = { Text("+52 123 456 7890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        isError = hasPhoneError,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        supportingText = {
                            if (hasPhoneError) {
                                Text("Número inválido", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    Button(
                        onClick = onAddNumber,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = newPhoneNumber.isNotBlank() && !hasPhoneError,
                        colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                }
            }

            if (isEnabled) {
                Text(
                    text = "Desactive el sistema para modificar la lista de números",
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
fun PhoneChip(number: String, onRemove: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = number,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (enabled) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// --- Message Configuration Card ---
@Composable
fun MessageCard(
    message: String,
    onMessageChange: (String) -> Unit,
    hasMessageError: Boolean,
    isEnabled: Boolean,
    onSave: () -> Unit,
    onTestSms: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mensaje de Respuesta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider()

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Mensaje de respuesta automática") },
                placeholder = { Text("Ej: No puedo atender ahora, te contacto más tarde.") },
                modifier = Modifier.fillMaxWidth(),
                isError = hasMessageError,
                enabled = !isEnabled,
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
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onTestSms,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = message.isNotBlank(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GradientStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Probar SMS", fontSize = 14.sp)
                }
            }

            if (isEnabled) {
                Text(
                    text = "Desactive el sistema para modificar el mensaje",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// --- Enhanced History Card with message text ---
@Composable
fun HistoryCard(context: Context) {
    val prefs = remember { context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE) }
    val historyJson = prefs.getString("message_history", "") ?: ""

    val history = remember(historyJson) {
        if (historyJson.isNotEmpty()) {
            historyJson.split("|||").mapNotNull { entry ->
                val parts = entry.split("||")
                when {
                    parts.size >= 3 -> HistoryEntry(parts[0], parts[1], parts[2])
                    parts.size >= 2 -> HistoryEntry(parts[0], parts[1], "")
                    else -> null
                }
            }.reversed()
        } else emptyList()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(
                    text = "${history.size} mensaje(s) en total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                history.take(10).forEachIndexed { index, entry ->
                    HistoryItem(entry)
                    if (index < minOf(history.size, 10) - 1) {
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
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = SuccessGreen
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
            if (entry.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${entry.message}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onRequestPermissions: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("AutoReplyPrefs", Context.MODE_PRIVATE) }
    var replyToPrivate by remember { mutableStateOf(prefs.getBoolean("reply_to_private", false)) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Permission statuses
    val hasPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val hasCallLog = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    val hasSendSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    val hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Permissions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permisos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider()

                PermissionItem("Leer estado del teléfono", hasPhoneState)
                PermissionItem("Leer registro de llamadas", hasCallLog)
                PermissionItem("Enviar SMS", hasSendSms)
                PermissionItem("Leer contactos", hasContacts)

                if (!hasPhoneState || !hasCallLog || !hasSendSms || !hasContacts) {
                    Button(
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                    ) {
                        Text("Solicitar Permisos Faltantes")
                    }
                } else {
                    Text(
                        text = "✓ Todos los permisos concedidos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Opciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider()

                // Reply to private/unknown calls toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Llamadas privadas",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Intentar responder cuando el número es privado/oculto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = replyToPrivate,
                        onCheckedChange = {
                            replyToPrivate = it
                            prefs.edit().putBoolean("reply_to_private", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        )
                    )
                }

                Divider()

                // Clear history button
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar historial de mensajes", color = MaterialTheme.colorScheme.error)
                }

                // Reset counter button
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resetear contador de mensajes", color = WarningOrange)
                }
            }
        }

        // About Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acerca de",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider()

                Text(
                    text = "AutoResponder Pro v2.0",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Aplicación de respuesta automática por SMS ante llamadas entrantes. " +
                            "Utiliza BroadcastReceiver para detectar el estado del teléfono " +
                            "y un Foreground Service para enviar los mensajes de forma confiable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Desarrolladores: Alejandro & Equipo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Clear history confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpiar historial") },
            text = { Text("¿Estás seguro de que deseas eliminar todo el historial de mensajes enviados?") },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().remove("message_history").apply()
                    showClearDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Historial limpiado")
                    }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reset counter confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Resetear contador") },
            text = { Text("¿Deseas poner el contador de mensajes enviados en 0?") },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().putInt("sent_messages_count", 0).apply()
                    showResetDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Contador reseteado")
                    }
                }) {
                    Text("Resetear", color = WarningOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PermissionItem(name: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isGranted) SuccessGreen else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isGranted) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.error
        )
    }
}

// --- Info Section ---
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
                text = "La app detecta llamadas entrantes según el modo configurado y responde automáticamente con el mensaje establecido."
            )
            InfoItem(
                icon = Icons.Default.Home,
                text = "Se requieren permisos de teléfono, SMS y contactos. Configure en ⚙ Ajustes."
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
    Row(verticalAlignment = Alignment.Top) {
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

// --- Data classes and utilities ---
data class HistoryEntry(val phoneNumber: String, val timestamp: String, val message: String = "")

fun isValidPhoneNumber(number: String): Boolean {
    if (number.isBlank()) return false
    val cleaned = number.replace("[^0-9+]".toRegex(), "")
    return cleaned.length >= 10 && cleaned.length <= 15
}
