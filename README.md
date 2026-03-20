# AutoResponder - Respuesta Automática de SMS

Aplicación Android moderna que permite responder de manera automática con un SMS cuando entra una llamada de un número específico configurado por el usuario.

## ✨ Características

- 📞 **Detección automática** de llamadas entrantes usando BroadcastReceiver registrado en el manifiesto
- ✉️ **Respuesta automática** con SMS personalizado
- 🎯 **Configuración flexible** de número objetivo y mensaje
- 🔛 **Activación/Desactivación** con un solo toque
- 📊 **Historial de mensajes** enviados con fecha y hora
- 🔔 **Notificaciones** de éxito/error en el envío
- 🎨 **Diseño moderno** con Material Design 3
- 🌙 **Soporte modo oscuro**

## 📋 Requisitos Cumplidos

✅ **BroadcastReceiver registrado en el manifiesto** - `PhoneCallReceiver` está declarado en `AndroidManifest.xml` con el filtro de intención `android.intent.action.PHONE_STATE`

✅ **Permisos necesarios configurados**:
- `READ_PHONE_STATE` - Detectar llamadas entrantes
- `READ_CALL_LOG` - Leer número entrante (requerido en Android 10+)
- `SEND_SMS` - Enviar mensajes de texto
- `FOREGROUND_SERVICE` - Ejecutar servicio de envío

✅ **Configuración desde aplicación**:
- Número telefónico objetivo ingresado por el usuario
- Mensaje de respuesta personalizable
- Toggle para activar/desactivar la funcionalidad

## 🏗️ Arquitectura

### Componentes Principales

| Componente | Descripción |
|------------|-------------|
| `MainActivity.kt` | Interfaz de usuario con configuración, historial y controles |
| `PhoneCallReceiver.kt` | BroadcastReceiver que detecta llamadas y verifica el número |
| `SmsSenderService.kt` | Servicio en primer plano que envía el SMS |

### Flujo de Datos

```
Llamada Entrante
    ↓
PhoneCallReceiver (BroadcastReceiver en Manifiesto)
    ↓
Verifica permisos y configuración
    ↓
Compara número entrante con configurado
    ↓
Inicia SmsSenderService (Foreground Service)
    ↓
Envía SMS automático
    ↓
Muestra notificación de confirmación
```

## 🚀 Uso

1. **Abrir la aplicación** - Se mostrará la pantalla principal con el estado del sistema
2. **Configurar número** - Ingrese el número telefónico que recibirá respuesta automática
3. **Escribir mensaje** - Escriba el mensaje que se enviará automáticamente (máx. 160 caracteres)
4. **Guardar** - Presione "Guardar Configuración"
5. **Activar** - Use el botón ACTIVAR para habilitar la respuesta automática
6. **Historial** - Revise el historial para ver mensajes enviados

## 📱 Permisos Requeridos

La aplicación requiere los siguientes permisos:

| Permiso | Uso |
|---------|-----|
| `READ_PHONE_STATE` | Detectar el estado del teléfono y llamadas entrantes |
| `READ_CALL_LOG` | Leer el número de teléfono entrante (requerido desde Android 10) |
| `SEND_SMS` | Enviar mensajes de texto automáticamente |
| `POST_NOTIFICATIONS` | Mostrar notificaciones (Android 13+) |
| `FOREGROUND_SERVICE` | Mantener el servicio activo mientras envía SMS |

> **Nota**: Todos los permisos se solicitan en tiempo de ejecución según las mejores prácticas de Android.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose con Material Design 3
- **Arquitectura**: Componentes de Android nativos
- **Almacenamiento**: SharedPreferences
- **APIs**: TelephonyManager, SmsManager, BroadcastReceiver, Foreground Service

## 📦 Estructura del Proyecto

```
app/src/main/java/com/example/broadcastreceiverytelefona/
├── MainActivity.kt           # UI principal con Compose
├── PhoneCallReceiver.kt      # BroadcastReceiver para llamadas
├── SmsSenderService.kt       # Servicio de envío de SMS
└── ui/theme/
    ├── Color.kt              # Esquema de colores personalizado
    ├── Theme.kt              # Tema Material 3
    └── Type.kt               # Tipografías

app/src/main/
├── AndroidManifest.xml        # Configuración con BroadcastReceiver
└── res/
    ├── drawable/              # Iconos y gráficos
    ├── mipmap-*/              # Iconos de la app
    ├── values/
    │   ├── strings.xml         # Textos de la aplicación
    │   └── themes.xml          # Temas de la app
    └── xml/                    # Reglas de backup
```

## ⚠️ Notas Importantes

- **Android 10+ (API 29+)**: Se requiere el permiso `READ_CALL_LOG` adicional para leer el número entrante en el BroadcastReceiver debido a restricciones de privacidad.
- **Servicio en Primer Plano**: El envío de SMS se realiza mediante un servicio en primer plano para garantizar que se complete incluso si la app está en segundo plano.
- **Normalización de números**: La app compara los últimos 10 dígitos del número para manejar diferentes formatos (con o sin código de país).

## 📝 Información del Desarrollador

**Alejandro Pérez Vázquez**

## 📄 Licencia

Este proyecto es parte de una práctica académica de Desarrollo de Aplicaciones Móviles.

---

**Versión**: 1.0  
**Fecha**: Marzo 2026
