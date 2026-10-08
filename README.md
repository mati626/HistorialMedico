# Aplicación Móvil para Android de Registro Personal de Historial Médico

Aplicación móvil diseñada para el registro, organización y seguimiento de la información de salud personal y de personas a cargo (hijos o adultos mayores) de manera centralizada y privada.

## Stack Tecnológico

- **Kotlin** + **Jetpack Compose** (interfaz de usuario)
- **Room** - persistencia local (base de datos SQLite en el dispositivo)
- **BiometricPrompt** + **EncryptedSharedPreferences** - autenticación y seguridad local
- **ML Kit Document Scanner** - escaneo y adjunto de documentos (recetas medicas y examenes)
- **AlarmManager** + **BroadcastReceiver** - recordatorios diarios de medicamentos y notificaciones previas a horas medicas
- **PdfDocument** (API nativa de Android, sin librerias externas) - generacion y exportacion del historial medico en PDF

## Arquitectura y Privacidad

Esta aplicación almacena toda la información de forma local en el dispositivo, sin backend ni servidor externo. La decisión considera que la Ley N° 19.628, modificada por la Ley N° 21.719,  clasifica los datos de salud como datos sensibles: mantenerlos en el dispositivo evita transmitirlos o almacenarlos en servidores externos.

El codigo esta organizado por paquetes segun responsabilidad:
- `ui/auth` - pantallas de bloqueo, creacion y validacion de PIN
- `ui/historial` - lista de perfiles, pantalla de detalle por perfil, y las secciones de Medicamentos, Horas Medicas y Examenes
- `data` - entidades y DAOs de Room
- `security` - biometria y manejo del PIN cifrado
- `alarms` - programacion de recordatorios y receivers de notificaciones
- `export` - generacion del PDF del historial

## Estrategia de Ramas (GitFlow)

Este proyecto sigue el modelo GitFlow:

- **main** - código estable
- **develop** - rama de integración, unión de módulos terminados
- **feature/autenticacion** - módulo de seguridad (biometría y PIN)
- **feature/historial-medico** - módulo de registro de historial médico
- **feature/alarmas-recordatorios** - módulo de notificaciones
- **feature/exportacion** - módulo de exportacion/compartición de datos

## Como Correr el Proyecto

1. Clonar el repositorio
2. Usar la rama 'main' (version estable) o 'develop' (version en desarrollo)
3. Abrir el proyecto en Android studio
4. Sincronizar Gradle (opcion: Sync Now)
5. Ejecutar en emulador o dispositivo físico con Android 7.0 (API 24) o superior

## Estado Actual

- **Autenticacion:** biometria y PIN de respaldo funcionando, con el PIN cifrado mediante EncryptedSharedPreferences (Android Keystore).
- **Historial medico:** funcional y probado, entidades `Perfil`, `Medicamento`, `HoraMedica` y `Examen` (Room), CRUD completo para cada una. Medicamentos y Examenes soportan escaneo y adjunto de documentos reales mediante ML Kit Document Scanner.
- **Alarmas y recordatorios:** terminado. Recordatorio diario por hora fija para medicamentos, y recordatorio puntual (fecha y hora) para horas medicas, ambos con notificación. Las notificaciones son de horario aproximado: Android puede retrasarlas algunos minutos para ahorrar bateria.
- **Exportacion:** terminado. Genera un PDF con el historial completo de un perfil (medicamentos, horas medicas y examenes, incluyendo las imagenes escaneadas adjuntas) y lo comparte mediante el selector de apps del sistema.
- **Integracion:** `feature/autenticacion`, `feature/historial-medico`, `feature/alarmas-recordatorios` y `feature/exportacion` ya fusionadas en `develop` - la app unifica login, registro de historial medico, recordatorios y exportacion en un solo flujo. `PerfilesScreen` se divide en una pantalla de lista y una pantalla de detalle por perfil, con navegacion entre ambas.
- **CI/CD**: GitHub Actions compila el proyecto automaticamente en cada push.


## Licencia

Este proyecto usa licencia MIT, -ver archivo [LICENSE](LICENSE)
