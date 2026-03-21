package com.example.broadcastreceiverytelefona

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

// ══════════════════════════════════════════════════════════════
// MainActivity
// ══════════════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { (permission, isGranted) ->
            when {
                isGranted -> { }
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(this, "El permiso $permission es necesario", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Permiso denegado. Configure en ajustes del sistema.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BroadcastReceiverYTelefoníaTheme {
                AutoReplyApp(onRequestPermissions = { requestPermissions() })
            }
        }
    }

    private fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val toRequest = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Navigation + Constants
// ══════════════════════════════════════════════════════════════

private enum class Screen { SPLASH, HOME, SETTINGS }

private object ResponseMode {
    const val ALL = "ALL"
    const val CONTACTS_ONLY = "CONTACTS_ONLY"
    const val UNKNOWN_ONLY = "UNKNOWN_ONLY"
    const val SPECIFIC_NUMBERS = "SPECIFIC_NUMBERS"
}

// SharedPreferences keys — MUST match PhoneCallReceiver/SmsSenderService
private const val PREFS_NAME = "AutoReplyPrefs"
private const val KEY_ENABLED = "auto_reply_enabled"
private const val KEY_MESSAGE = "auto_reply_message"
private const val KEY_RESPONSE_MODE = "response_mode"
private const val KEY_TARGET_NUMBERS = "target_numbers"
private const val KEY_REPLY_PRIVATE = "reply_to_private"
private const val KEY_SENT_COUNT = "sent_messages_count"
private const val KEY_HISTORY = "message_history"

// ══════════════════════════════════════════════════════════════
// Number list helpers (JSON array in SharedPreferences)
// ══════════════════════════════════════════════════════════════

private fun loadNumbers(prefs: android.content.SharedPreferences): List<String> {
    val json = prefs.getString(KEY_TARGET_NUMBERS, "[]") ?: "[]"
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (_: Exception) {
        val old = prefs.getString("target_number", "")
        if (!old.isNullOrBlank()) listOf(old) else emptyList()
    }
}

private fun saveNumbers(prefs: android.content.SharedPreferences, list: List<String>) {
    val arr = JSONArray()
    list.forEach { arr.put(it) }
    prefs.edit().putString(KEY_TARGET_NUMBERS, arr.toString()).apply()
}

private fun isValidPhone(number: String): Boolean {
    if (number.isBlank()) return false
    val cleaned = number.replace("[^0-9+]".toRegex(), "")
    return cleaned.length in 8..15
}

// ══════════════════════════════════════════════════════════════
// Root Composable
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReplyApp(onRequestPermissions: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf(Screen.SPLASH) }

    when (screen) {
        Screen.SPLASH -> {
            SplashScreen(onTimeout = { screen = Screen.HOME })
        }
        else -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    "AutoResponder Pro",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    when (screen) {
                                        Screen.HOME -> "Asistente Inteligente de Llamadas"
                                        Screen.SETTINGS -> "Ajustes"
                                        else -> ""
                                    },
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
                            if (screen == Screen.SETTINGS) {
                                IconButton(onClick = { screen = Screen.HOME }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                                }
                            }
                        },
                        actions = {
                            if (screen == Screen.HOME) {
                                IconButton(onClick = { screen = Screen.SETTINGS }) {
                                    Icon(Icons.Default.Settings, "Ajustes", tint = Color.White)
                                }
                            }
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbar) },
                containerColor = MaterialTheme.colorScheme.background
            ) { pad ->
                when (screen) {
                    Screen.HOME -> HomeScreen(Modifier.padding(pad), context, snackbar, scope)
                    Screen.SETTINGS -> SettingsScreen(Modifier.padding(pad), context, snackbar, scope, onRequestPermissions)
                    else -> {}
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// SPLASH Screen
// ══════════════════════════════════════════════════════════════

@Composable
private fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AutoResponder Pro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
// HOME Screen
// ══════════════════════════════════════════════════════════════

@Composable
private fun HomeScreen(
    modifier: Modifier,
    context: Context,
    snackbar: SnackbarHostState,
    scope: CoroutineScope
) {
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var isEnabled by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var msgError by remember { mutableStateOf(false) }
    var responseMode by remember { mutableStateOf(ResponseMode.SPECIFIC_NUMBERS) }
    var numbersList by remember { mutableStateOf(listOf<String>()) }
    var newPhone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }
    var sentCount by remember { mutableIntStateOf(0) }
    var showHistory by remember { mutableStateOf(false) }
    var testPhone by remember { mutableStateOf("") }

    // Load saved state
    LaunchedEffect(Unit) {
        isEnabled = prefs.getBoolean(KEY_ENABLED, false)
        message = prefs.getString(KEY_MESSAGE, "") ?: ""
        responseMode = prefs.getString(KEY_RESPONSE_MODE, ResponseMode.SPECIFIC_NUMBERS) ?: ResponseMode.SPECIFIC_NUMBERS
        numbersList = loadNumbers(prefs)
        sentCount = prefs.getInt(KEY_SENT_COUNT, 0)
    }

    val canEnable = message.isNotBlank() && !msgError &&
            (responseMode != ResponseMode.SPECIFIC_NUMBERS || numbersList.isNotEmpty())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Status Hero Card ───
        StatusHeroCard(
            isEnabled = isEnabled,
            canEnable = canEnable,
            sentCount = sentCount,
            responseMode = responseMode,
            onToggle = {
                if (canEnable || isEnabled) {
                    isEnabled = !isEnabled
                    prefs.edit().putBoolean(KEY_ENABLED, isEnabled).apply()
                    scope.launch {
                        snackbar.showSnackbar(
                            if (isEnabled) "✅ Respuesta automática ACTIVADA"
                            else "⏸ Respuesta automática DESACTIVADA",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    Toast.makeText(context, "Por favor complete la configuración primero", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // ─── Response Mode Selector ───
        ResponseModeCard(
            selectedMode = responseMode,
            locked = isEnabled,
            onModeSelected = { mode ->
                responseMode = mode
                prefs.edit().putString(KEY_RESPONSE_MODE, mode).apply()
            }
        )

        // ─── Specific Numbers (Chips) ───
        AnimatedVisibility(
            visible = responseMode == ResponseMode.SPECIFIC_NUMBERS,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            NumbersChipCard(
                numbers = numbersList,
                newPhone = newPhone,
                phoneError = phoneError,
                locked = isEnabled,
                onNewPhoneChange = { newPhone = it; phoneError = false },
                onAdd = {
                    val cleaned = newPhone.trim()
                    when {
                        !isValidPhone(cleaned) -> phoneError = true
                        numbersList.any { it == cleaned } -> {
                            scope.launch { snackbar.showSnackbar("Ese número ya está en la lista") }
                        }
                        else -> {
                            numbersList = numbersList + cleaned
                            saveNumbers(prefs, numbersList)
                            newPhone = ""
                            scope.launch { snackbar.showSnackbar("Número agregado ✓") }
                        }
                    }
                },
                onRemove = { num ->
                    numbersList = numbersList.filter { it != num }
                    saveNumbers(prefs, numbersList)
                }
            )
        }

        // ─── Message ───
        MessageCard(
            message = message,
            error = msgError,
            locked = isEnabled,
            onMessageChange = { message = it; msgError = it.length > 160 },
            onSave = {
                if (message.isNotBlank() && message.length <= 160) {
                    prefs.edit().putString(KEY_MESSAGE, message).apply()
                    scope.launch { snackbar.showSnackbar("✅ Mensaje guardado") }
                } else {
                    scope.launch { snackbar.showSnackbar("Verifique el mensaje") }
                }
            }
        )

        // ─── Test SMS ───
        TestSmsCard(
            testPhone = testPhone,
            onPhoneChange = { testPhone = it },
            message = message,
            onSend = {
                val target = testPhone.trim()
                if (isValidPhone(target) && message.isNotBlank()) {
                    val intent = Intent(context, SmsSenderService::class.java).apply {
                        putExtra(SmsSenderService.EXTRA_PHONE_NUMBER, target)
                        putExtra(SmsSenderService.EXTRA_MESSAGE, message)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    scope.launch { snackbar.showSnackbar("Enviando SMS de prueba a $target...") }
                } else {
                    scope.launch { snackbar.showSnackbar("Ingrese un número válido y un mensaje") }
                }
            }
        )

        // ─── History ───
        OutlinedButton(
            onClick = { showHistory = !showHistory },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (showHistory) "Ocultar historial" else "📜 Ver historial de mensajes",
                fontWeight = FontWeight.Medium
            )
        }

        AnimatedVisibility(
            visible = showHistory,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            HistoryCard(context)
        }

        // ─── Info ───
        InfoSection()

        Spacer(Modifier.height(24.dp))
    }
}

// ══════════════════════════════════════════════════════════════
// Status Hero Card
// ══════════════════════════════════════════════════════════════

@Composable
private fun StatusHeroCard(
    isEnabled: Boolean,
    canEnable: Boolean,
    sentCount: Int,
    responseMode: String,
    onToggle: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue = if (isEnabled) 1.1f else 1f,
        animationSpec = tween(300),
        label = "iconPulse"
    )

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
                    Brush.verticalGradient(
                        if (isEnabled) listOf(SuccessGreen, SuccessGreen.copy(alpha = 0.7f))
                        else listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    if (isEnabled) "Sistema Activo" else "Sistema Inactivo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    when {
                        isEnabled -> "Modo: ${modeLabel(responseMode)}"
                        else -> "Configure y active para responder automáticamente"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.85f)
                )

                if (sentCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "$sentCount mensajes enviados",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onToggle,
                    enabled = canEnable || isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) Color.White.copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        if (isEnabled) Icons.Default.Close else Icons.Default.PlayArrow,
                        null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEnabled) "DESACTIVAR" else "ACTIVAR",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun modeLabel(mode: String) = when (mode) {
    ResponseMode.ALL -> "Todas las llamadas"
    ResponseMode.CONTACTS_ONLY -> "Solo contactos"
    ResponseMode.UNKNOWN_ONLY -> "Solo desconocidos"
    else -> "Números específicos"
}

// ══════════════════════════════════════════════════════════════
// Response Mode Card
// ══════════════════════════════════════════════════════════════

@Composable
private fun ResponseModeCard(
    selectedMode: String,
    locked: Boolean,
    onModeSelected: (String) -> Unit
) {
    SectionCard(icon = Icons.Default.Phone, title = "¿A quién responder?") {
        val modes = listOf(
            Triple(ResponseMode.ALL, "Todas las llamadas", "Responde a cualquier número"),
            Triple(ResponseMode.CONTACTS_ONLY, "Solo contactos", "Solo números guardados en tu dispositivo"),
            Triple(ResponseMode.UNKNOWN_ONLY, "Solo desconocidos", "Números que NO están en contactos"),
            Triple(ResponseMode.SPECIFIC_NUMBERS, "Números específicos", "Solo los números de tu lista")
        )

        modes.forEach { (mode, title, desc) ->
            ModeOption(
                title = title,
                description = desc,
                selected = selectedMode == mode,
                enabled = !locked,
                onClick = { onModeSelected(mode) }
            )
        }

        if (locked) {
            LockHint("Desactive el sistema para cambiar el modo")
        }
    }
}

@Composable
private fun ModeOption(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "optBg"
    )
    val border by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "optBorder"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            border
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (selected) Icons.Default.Check else Icons.Default.PlayArrow,
                null,
                modifier = Modifier.size(22.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Numbers Chip Card
// ══════════════════════════════════════════════════════════════

@Composable
private fun NumbersChipCard(
    numbers: List<String>,
    newPhone: String,
    phoneError: Boolean,
    locked: Boolean,
    onNewPhoneChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    SectionCard(icon = Icons.Default.AccountCircle, title = "Números Específicos") {

        if (numbers.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                numbers.forEach { num ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                num,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (!locked) {
                                IconButton(onClick = { onRemove(num) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Close, "Eliminar", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                "No hay números. Agregue al menos uno.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (!locked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newPhone,
                    onValueChange = onNewPhoneChange,
                    label = { Text("Número de teléfono") },
                    placeholder = { Text("+52 123 456 7890") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onAdd() }),
                    modifier = Modifier.weight(1f),
                    isError = phoneError,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    supportingText = {
                        if (phoneError) Text("Número inválido (mín. 8 dígitos)", color = MaterialTheme.colorScheme.error)
                    }
                )
                Button(
                    onClick = onAdd,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = newPhone.isNotBlank() && !phoneError,
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Icon(Icons.Default.Add, "Agregar", tint = Color.White)
                }
            }
        }

        if (locked) LockHint("Desactive el sistema para editar números")
    }
}

// ══════════════════════════════════════════════════════════════
// Message Card
// ══════════════════════════════════════════════════════════════

@Composable
private fun MessageCard(
    message: String,
    error: Boolean,
    locked: Boolean,
    onMessageChange: (String) -> Unit,
    onSave: () -> Unit
) {
    SectionCard(icon = Icons.Default.Edit, title = "Mensaje de Respuesta") {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            label = { Text("Mensaje automático") },
            placeholder = { Text("No puedo atender ahora, te contacto más tarde.") },
            modifier = Modifier.fillMaxWidth(),
            isError = error,
            enabled = !locked,
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(14.dp),
            supportingText = {
                val rem = 160 - message.length
                Text(
                    "$rem caracteres restantes",
                    color = when {
                        rem < 0 -> MaterialTheme.colorScheme.error
                        rem < 20 -> WarningOrange
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            enabled = !locked && message.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
        ) {
            Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text("Guardar Mensaje", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (locked) LockHint("Desactive el sistema para editar el mensaje")
    }
}

// ══════════════════════════════════════════════════════════════
// Test SMS Card
// ══════════════════════════════════════════════════════════════

@Composable
private fun TestSmsCard(
    testPhone: String,
    onPhoneChange: (String) -> Unit,
    message: String,
    onSend: () -> Unit
) {
    SectionCard(icon = Icons.AutoMirrored.Filled.Send, title = "Prueba de Envío") {
        Text(
            "Envía un SMS de prueba para verificar que todo funciona correctamente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = testPhone,
            onValueChange = onPhoneChange,
            label = { Text("Número de destino") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )

        Button(
            onClick = onSend,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            enabled = testPhone.isNotBlank() && message.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Enviar SMS de Prueba", fontWeight = FontWeight.Bold)
        }
    }
}

// ══════════════════════════════════════════════════════════════
// History Card (reads phone||timestamp||message format)
// ══════════════════════════════════════════════════════════════

@Composable
private fun HistoryCard(context: Context) {
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val raw = prefs.getString(KEY_HISTORY, "") ?: ""

    data class Entry(val phone: String, val time: String, val msg: String)

    val history = remember(raw) {
        if (raw.isEmpty()) emptyList()
        else raw.split("|||").mapNotNull { chunk ->
            val p = chunk.split("||")
            when {
                p.size >= 3 -> Entry(p[0], p[1], p[2])
                p.size >= 2 -> Entry(p[0], p[1], "")
                else -> null
            }
        }.reversed()
    }

    SectionCard(icon = Icons.Default.DateRange, title = "Historial de Mensajes") {
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Menu, null,
                    Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(8.dp))
                Text("No hay mensajes enviados aún", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text(
                "${history.size} mensaje(s) en total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            history.take(15).forEachIndexed { idx, e ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Check, null,
                        Modifier
                            .size(20.dp)
                            .padding(top = 2.dp),
                        tint = SuccessGreen
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(e.phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(e.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (e.msg.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "\"${e.msg}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (idx < minOf(history.size, 15) - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// SETTINGS Screen
// ══════════════════════════════════════════════════════════════

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    context: Context,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    onRequestPermissions: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var replyPrivate by remember { mutableStateOf(prefs.getBoolean(KEY_REPLY_PRIVATE, false)) }
    var showClearDlg by remember { mutableStateOf(false) }
    var showResetDlg by remember { mutableStateOf(false) }

    val perms = listOf(
        "Leer estado del teléfono" to Manifest.permission.READ_PHONE_STATE,
        "Registro de llamadas" to Manifest.permission.READ_CALL_LOG,
        "Enviar SMS" to Manifest.permission.SEND_SMS,
        "Leer contactos" to Manifest.permission.READ_CONTACTS
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Permisos ───
        SectionCard(icon = Icons.Default.Info, title = "Permisos") {
            perms.forEach { (label, perm) ->
                val ok = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (ok) Icons.Default.Check else Icons.Default.Close,
                        null,
                        Modifier.size(20.dp),
                        tint = if (ok) SuccessGreen else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (ok) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                }
            }

            val allGranted = perms.all {
                ContextCompat.checkSelfPermission(context, it.second) == PackageManager.PERMISSION_GRANTED
            }

            if (!allGranted) {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Text("Solicitar Permisos Faltantes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Text(
                    "✓ Todos los permisos concedidos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ─── Opciones ───
        SectionCard(icon = Icons.Default.Settings, title = "Opciones") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Llamadas privadas", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        "Intentar responder a números ocultos/privados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = replyPrivate,
                    onCheckedChange = {
                        replyPrivate = it
                        prefs.edit().putBoolean(KEY_REPLY_PRIVATE, it).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            OutlinedButton(
                onClick = { showClearDlg = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text("Limpiar historial", color = MaterialTheme.colorScheme.error)
            }

            OutlinedButton(
                onClick = { showResetDlg = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange)
            ) {
                Icon(Icons.Default.Clear, null, Modifier.size(18.dp), tint = WarningOrange)
                Spacer(Modifier.width(8.dp))
                Text("Resetear contador", color = WarningOrange)
            }
        }

        // ─── Acerca de ───
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
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Acerca de", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text("AutoResponder Pro v2.0", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "Respuesta automática por SMS ante llamadas entrantes. " +
                            "Usa BroadcastReceiver y Foreground Service para enviar mensajes de forma confiable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // Dialogs
    if (showClearDlg) {
        AlertDialog(
            onDismissRequest = { showClearDlg = false },
            title = { Text("Limpiar historial") },
            text = { Text("¿Eliminar todo el historial de mensajes enviados?") },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().remove(KEY_HISTORY).apply()
                    showClearDlg = false
                    scope.launch { snackbar.showSnackbar("Historial limpiado") }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearDlg = false }) { Text("Cancelar") } }
        )
    }

    if (showResetDlg) {
        AlertDialog(
            onDismissRequest = { showResetDlg = false },
            title = { Text("Resetear contador") },
            text = { Text("¿Poner el contador de mensajes enviados en 0?") },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().putInt(KEY_SENT_COUNT, 0).apply()
                    showResetDlg = false
                    scope.launch { snackbar.showSnackbar("Contador reseteado") }
                }) { Text("Resetear", color = WarningOrange) }
            },
            dismissButton = { TextButton(onClick = { showResetDlg = false }) { Text("Cancelar") } }
        )
    }
}

// ══════════════════════════════════════════════════════════════
// Reusable Section Card
// ══════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
private fun LockHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// ══════════════════════════════════════════════════════════════
// Info Section
// ══════════════════════════════════════════════════════════════

@Composable
private fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoRow(Icons.Default.Info, "Detecta llamadas según el filtro configurado y responde con SMS automáticamente.")
            InfoRow(Icons.Default.Home, "Permisos de teléfono, SMS y contactos son necesarios. Configúralos en ⚙ Ajustes.")
            InfoRow(Icons.Default.Notifications, "Recibirás una notificación cada vez que se envíe un SMS.")
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
