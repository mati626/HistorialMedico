# Aplicación Móvil para Android de Registro Personal de Historial Médico

Aplicación móvil diseñada para el registro, organización y seguimiento de la información de salud personal y de personas a cargo (hijos o adultos mayores) de manera centralizada y privada.

## Stack Tecnológico

- **Kotlin** + **Jetpack Compose** (interfaz de usuario)
- **Room** - persistencia local (base de datos SQLite en el dispositivo)
- **BiometricPrompt** + **EncryptedSharedPreferences** - autenticación y seguridad local
- **ML Kit Document Scanner** - escaneo y adjunto de documentos (recetas medicas y examenes)
- **AlarmManager** + **BroadcastReceiver** - recordatorios diarios de medicamentos y notificaciones previas a horas medicas
- **PdfDocument** (API nativa de Android, sin librerias externas) - generacion y exportacion del historial medico en PDF
- **JUnit**, **Compose UI Test** y **Espresso** - pruebas unitarias e instrumentadas

## Arquitectura y Privacidad

Esta aplicación almacena toda la información de forma local en el dispositivo, sin backend ni servidor externo. La decisión considera que la Ley N° 19.628, modificada por la Ley N° 21.719, clasifica los datos de salud como datos sensibles: mantenerlos en el dispositivo evita transmitirlos o almacenarlos en servidores externos. Esto no constituye una declaración de cumplimiento normativo.

Seguridad actual: el PIN se guarda como hash SHA-256 dentro de EncryptedSharedPreferences (Android Keystore). La base de datos Room **no esta cifrada** (pendiente).

El codigo esta organizado por paquetes segun responsabilidad:
- `ui/auth` - pantallas de bloqueo, creacion y validacion de PIN
- `ui/perfil` - pantalla inicial para crear el perfil del titular
- `ui/historial` - lista de pacientes, detalle por paciente, y las secciones de Medicamentos (con el control de tomas y el recuento de pastillas), Horas Medicas, Examenes y presion arterial
- `ui/components` - selectores reutilizables de fecha y hora
- `data` - entidades y DAOs de Room, y reglas de los exámenes y del recuento de pastillas.
- `security` - biometría, manejo del PIN cifrado y regla para crear el PIN
- `alarms` - programacion de recordatorios y receivers de notificaciones
- `export` - generacion del PDF del historial
- `util` - utilidades de formato y conversión de fechas, reglas de texto compartidas por los formularios y guardado de los documentos escaneados

## Modelo de datos

La base de datos esta en la versión 8. Un **Perfil** representa al titular del dispositivo y agrupa a sus **Pacientes** (el propio titular y/o personas a su cargo). 
De cada paciente dependen sus **Medicamentos**, **Horas Medicas**, **Exámenes** y registros de **Presión Arterial**. Los recordatorios se guardan en la tabla **Alarma** (varias por medicamento, o una por hora medica) y las tomas en **ControlToma**. Un medicamento con recordatorios puede guardar cuántas pastillas quedan y cuántas se toman en cada toma.

Desde la versión 7 el esquema se exporta en `app/schemas` y los cambios de estructura se hacen con migraciones. La versión 8 agrega el conteo de pastillas mediante una migración automática de Room desde la 7, que conserva los datos existentes El borrado destructivo queda limitado a las versiones anteriores a la 7.

## Estrategia de Ramas (GitFlow)

Este proyecto sigue el modelo GitFlow:

- **main** - código estable
- **develop** - rama de integración, unión de módulos terminados
- **feature/autenticacion** - módulo de seguridad (biometría y PIN)
- **feature/historial-medico** - módulo de registro de historial médico
- **feature/alarmas-recordatorios** - módulo de notificaciones
- **feature/exportacion** - módulo de exportacion/compartición de datos
- **feature/room-v7** - rediseño de la base de datos (pacientes, alarmas y nuevas tablas)
- **feature/presion-arterial** - registro de presión arterial
- **feature/recordatorios-horas-medicas** - anticipacion del recordatorio y reprogramacion de alarmas
- **feature/control-tomas** - control diario de tomas de medicamentos
- **feature/pruebas-e2e** - pruebas de extremo a extremo del flujo de exportacion a PDF
- **feature/pruebas-unitarias** - pruebas unitarias de fechas, alarmas y regla del PIN
- **feature/pruebas-integracion** - pruebas de integración de la base de datos y del PIN
- **feature/prueba-e2e-presion** - pruebas de extremo a extremo del registro de presión arterial
- **feature/validacion-entradas** - validación de los campos de texto (largo máximo, caracteres permitidos y espacios) y sus pruebas
- **feature/solicitudes-examenes** - estado próximo o realizado de los exámenes y consulta en que se pidió cada uno
- **fix/documentos-escaneados** - guardado de los documentos escaneados fuera de la caché y su borrado junto con el registro
- **feature/recuento-pastillas** - conteo de pastillas, aviso cuando quedan pocas y reposición 

## Como Correr el Proyecto

1. Clonar el repositorio
2. Usar la rama 'main' (version estable) o 'develop' (version en desarrollo)
3. Abrir el proyecto en Android studio
4. Sincronizar Gradle (opcion: Sync Now)
5. Ejecutar en emulador o dispositivo físico con Android 7.0 (API 24) o superior

## Estado Actual

- **Autenticación:** biometría y PIN de respaldo funcionando.
- **Perfil y pacientes:** en el primer inicio se crea el perfil del titular, con la opción de registrarse también como paciente. Desde la lista se agregan y eliminan pacientes.
- **Historial médico:** registro y eliminación de medicamentos, horas médicas y exámenes por paciente. Medicamentos y exámenes permiten escanear y adjuntar documentos con ML Kit Document Scanner. Los documentos escaneados se copian al almacenamiento interno de la aplicación, para que no se pierdan si se borra el caché, y se eliminan junto con el medicamento, examen o paciente al que pertenecen. Las fechas y horas se eligen con calendario y reloj.
- **Solicitudes de exámenes:** al registrar un examen se indica si es próximo o ya fue realizado. Un examen próximo puede quedar sin fecha (solicitado) o con una fecha que no sea pasada (agendado); uno realizado exige una fecha que no sea futura y admite un resultado opcional. Un examen próximo se puede marcar como realizado desde su tarjeta. Opcionalmente se indica la consulta en que se pidió, elegida entre las horas medicas ya ocurridas del paciente; la fecha del examen no puede ser anterior a esa consulta, y la consulta se muestra en la tarjeta y en el PDF. Las reglas estan en `data/ReglasExamen.kt`.
- **Presión arterial:** registro de mediciones por paciente (sistólica, diastólica, pulso opcional, fecha y hora), con validación de rangos. La aplicación registra y muestra valores, no los interpreta.
- **Control de tomas:** los medicamentos con recordatorios muestran una casilla por cada hora para marcar la toma del día, junto al resumen "Tomas de hoy: X de N". Las marcas se guardan por día: al cambiar el día, las casillas parten vacias.
- **Recuento de pastillas:** al agregar un medicamento con recordatorios se puede indicar cuántas pastillas quedan y cuántas se toman en cada toma. Marcar una toma descuenta esas pastillas y desmarcarla las devuelve; tocar la casilla varias veces seguidas no descuenta de más. La tarjeta muestra para cuántos dias alcanzan y avisa "Reponga pronto" cuando alcanzan para 5 dias o menos, y el mismo aviso se agrega a la notificacion del recordatorio. EL botón Reponer suma las pastillas compradas. Las reglas están en `data/ReglasPastillas.kt`.
- **Alarmas y recordatorios:** varios recordatorios diarios por medicamento y un recordatorio por hora médica, eligiendo con cuánta anticipación avisar (30 minutos, 1 hora, 2 horas o 1 día). Las notificaciones son de horario aproximado: Android puede retrasarlas hasta una hora para ahorrar batería, y en algunos fabricantes, como Samsung, pueden quedar retenidas mientras la aplicacion esta congelada en segundo plano. Las alarmas se reprograman al reiniciar el dispositivo, al actualizar la aplicación y al volver a abrirla.
- **Validación de entradas:** los campos de texto libre (nombres, relación, medicamento, dosis, especialidad, lugar, tipo y resultado de examen) quitan los espacios sobrantes, tienen un largo máximo con contador y aceptan solo los caracteres que corresponden a cada campo (por ejemplo, el nombre de una persona solo admite letras, espacios, apostrofo y guion). Las reglas están en `util/ReglasTexto.kt` y las usan todos los formularios.
- **Exportación:** genera un PDF con el historial completo de un paciente (medicamentos, horas médicas, exámenes y presion arterial, incluidas las imágenes escaneadas) y lo comparte mediante el selector de apps del sistema.
- **CI/CD:** GitHub Actions compila el proyecto y ejecuta las pruebas unitarias en cada push.
- **Pruebas:**
  - 47 casos unitarios (conversión de fechas, reglas de las alarmas, reglas del PIN, reglas del estado y la fecha de los examenes, reglas de recuento de pastillas, y reglas de texto con entradas invalidas: vacías, solo espacios, demasiado largas, con números, emojis o símbolos), que el CI ejecuta en cada push
  - 9 casos de integracion sobre la base de datos (creacion del perfil, medicamentos con alarmas y borrado en cascada de un paciente) y sobre el PIN cifrado.
  - 2 pruebas de extremo a extremo que entran con PIN: exportar el PDF de un paciente y registrar una presion arterial.
  - Las de integración y extremo a extremo se ejecutan desde Android Studio en un emulador; el CI no las ejecuta.
  - las de extremo a extremo borran los datos de la aplicacion en el dispositivo donde se ejecutan.

## En Desarrollo y Pendiente
- **Recordatorios en segundo plano** evitar que el ahorro de batería del sistema o del fabricante los atrase o los retenga.
- **Ingreso de medicamentos por reconocimiento de texto** de la receta y la caja.
- **Acceso simplificado para emergencias.**
- **Seguridad:** cifrado de la base de datos y límite de intentos de PIN.
  
## Licencia

Este proyecto usa licencia MIT, ver archivo [LICENSE](LICENSE)
