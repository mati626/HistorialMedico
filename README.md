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

Esta aplicación almacena toda la información de forma local en el dispositivo, sin backend ni servidor externo. La decisión considera que la Ley N° 19.628, modificada por la Ley N° 21.719,  clasifica los datos de salud como datos sensibles: mantenerlos en el dispositivo evita transmitirlos o almacenarlos en servidores externos. Esto no constituye una declaración de cumplimiento normativo.

Seguridad actual: el PIN se guarda como hash SHA-256 dentro de EncryptedSharedPreferences (Android Keystore). La base de datos Room **no esta cifrada** (pendiente).

El codigo esta organizado por paquetes segun responsabilidad:
- `ui/auth` - pantallas de bloqueo, creacion y validacion de PIN
- `ui/perfil` - pantalla inicial para crear el perfil del titular
- `ui/historial` - lista de pacientes, detalle por paciente, y las secciones de Medicamentos, Horas Medicas y Examenes
- `ui/components` - selectores reutilizables de fecha y hora
- `data` - entidades y DAOs de Room
- `security` - biometria y manejo del PIN cifrado
- `alarms` - programacion de recordatorios y receivers de notificaciones
- `export` - generacion del PDF del historial
- `util` - utilidades de formato y conversión de fechas

## Modelo de datos

La base de datos esta en la versión 7. Un **Perfil** representa al titular del dispositivo y agrupa a sus **Pacientes** (el propio titular y/o personas a su cargo). 
De cada paciente dependen sus **Medicamentos**, **Horas Medicas**, **Exámenes** y registros de **Presión Arterial**. Los recordatorios se guardan en la tabla **Alarma** (varias por medicamento, o una por hora medica) y las tomas en **ControlToma**

Desde la version 7 el esquema se exporta en `app/schemas` y los cambios de estructura se hacen con migraciones. El borrado destructivo queda limitado a las versiones anteriores a la 7.

## Estrategia de Ramas (GitFlow)

Este proyecto sigue el modelo GitFlow:

- **main** - código estable
- **develop** - rama de integración, unión de módulos terminados
- **feature/autenticacion** - módulo de seguridad (biometría y PIN)
- **feature/historial-medico** - módulo de registro de historial médico
- **feature/alarmas-recordatorios** - módulo de notificaciones
- **feature/exportacion** - módulo de exportacion/compartición de datos
- **feature/room-v7** - rediseño de la base de datos (pacientes, alarmas y nuevas tablas)

## Como Correr el Proyecto

1. Clonar el repositorio
2. Usar la rama 'main' (version estable) o 'develop' (version en desarrollo)
3. Abrir el proyecto en Android studio
4. Sincronizar Gradle (opcion: Sync Now)
5. Ejecutar en emulador o dispositivo físico con Android 7.0 (API 24) o superior

# Estado Actual

- **Autenticación:** biometría y PIN de respaldo funcionando.
- **Perfil y pacientes:** en el primer inicio se crea el perfil del titular, con la opción de registrarse también como paciente. Desde la lista se agregan y eliminan pacientes.
- **Historial médico:** registro y eliminación de medicamentos, horas médicas y exámenes por paciente. Medicamentos y exámenes permiten escanear y adjuntar documentos con ML Kit Document Scanner. Las fechas y horas se eligen con calendario y reloj.
- **Alarmas y recordatorios:** varios recordatorios diarios por medicamento y un recordatorio puntual por hora médica, ambos con notificación. Las notificaciones son de horario aproximado: Android puede retrasarlas algunos minutos para ahorrar batería. Las alarmas se reprograman automáticamente si el dispositivo se reinicia.
- **Exportación:** genera un PDF con el historial completo de un paciente (medicamentos, horas médicas y exámenes, incluidas las imágenes escaneadas) y lo comparte mediante el selector de apps del sistema.
- **CI/CD:** GitHub Actions compila el proyecto automáticamente en cada push.

## En Desarrollo y Pendiente

- **Presión arterial y control diario de tomas:** las tablas ya existen en la base de datos, pero todavía no tienen pantalla.
- **Solicitudes de exámenes:** el estado del examen (solicitado, agendado, realizado) hoy se deduce de los datos ingresados; falta el selector de estado y el vínculo con una hora médica.
- **Ingreso de medicamentos por reconocimiento de texto** de la receta y la caja.
- **Acceso simplificado para emergencias.**
- **Seguridad:** cifrado de la base de datos y límite de intentos de PIN.
- **Alarmas:** al actualizar o reinstalar la aplicación, los recordatorios no se reprograman hasta reiniciar el dispositivo.


## Licencia

Este proyecto usa licencia MIT, -ver archivo [LICENSE](LICENSE)
