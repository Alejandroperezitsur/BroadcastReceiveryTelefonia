# Aplicación Android - Respuesta Automática de SMS

## Descripción
Aplicación Android que permite responder de manera automática con un SMS cuando entra una llamada de un número específico configurado por el usuario.

## Características
- Detección automática de llamadas entrantes usando BroadcastReceiver
- Configuración de número telefónico objetivo
- Personalización del mensaje de respuesta automática
- Almacenamiento persistente de la configuración
- Interfaz de usuario intuitiva con Jetpack Compose

## Componentes Principales

### 1. PhoneCallReceiver.kt
- BroadcastReceiver que escucha cambios en el estado telefónico
- Detecta cuando entra una llamada (EXTRA_STATE_RINGING)
- Compara el número entrante con el configurado
- Inicia el servicio de envío de SMS si coincide

### 2. SmsSenderService.kt
- Servicio que envía SMS automáticamente
- Utiliza SmsManager para el envío de mensajes
- Maneja errores y muestra notificaciones

### 3. MainActivity.kt
- Interfaz de usuario para configuración
- Campos para número telefónico y mensaje
- Almacenamiento usando SharedPreferences

## Permisos Requeridos
- `READ_PHONE_STATE`: Para detectar llamadas entrantes
- `SEND_SMS`: Para enviar mensajes de texto
- `READ_CALL_LOG`: Para acceder al historial de llamadas
- `READ_PHONE_NUMBERS`: Para leer números telefónicos

## Configuración en AndroidManifest.xml
- BroadcastReceiver registrado para ACTION_PHONE_STATE_CHANGED
- Servicio declarado para envío de SMS
- Permisos necesarios configurados

## Uso
1. Abrir la aplicación
2. Ingresar el número telefónico que responderá automáticamente
3. Escribir el mensaje de respuesta
4. Guardar la configuración
5. La aplicación responderá automáticamente cuando ese número llame

## Arquitectura
- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose
- **Arquitectura**: Componentes de Android nativos
- **Almacenamiento**: SharedPreferences

## API Utilizadas
- TelephonyManager.ACTION_PHONE_STATE_CHANGED
- SmsManager para envío de SMS
- BroadcastReceiver para detección de eventos

## Notas Importantes
- La aplicación requiere permisos explícitos en Android 6.0+
- El usuario debe conceder permisos de teléfono y SMS
- Funciona en segundo plano gracias al BroadcastReceiver registrado en el manifiesto
