# Aplicación Móvil para Android de Registro Personal de Historial Médico

Aplicación móvil diseñada para el registro, organización y seguimiento de la información de salud personal y de personas a cargo (hijos o adultos mayores) de manera centralizada y privada.

## Stack Tecnológico

- **Kotlin** + **Jetpack Compose** (interfaz de usuario)
- **Room** - persistencia local (base de datos SQLite en el dispositivo)
- **BiometricPrompt** + **EncryptedSharedPreferences** - autenticación y seguridad local
- **SQLCipher for Android** - cifrado de la base de datos Room, con la clave protegida por Android Keystore
- **Docker** - compilación del APK en un contenedor
- **ML Kit Document Scanner** - escaneo y adjunto de documentos (recetas medicas y examenes)
- **ML Kit Text Recognition** - lectura en el dispositivo del texto de recetas, cajas de medicamentos y papeles con la presión arterial
- **AlarmManager** + **BroadcastReceiver** - recordatorios diarios de medicamentos y notificaciones previas a horas medicas
- **PdfDocument** (API nativa de Android, sin librerias externas) - generacion y exportacion del historial medico en PDF
- **JUnit**, **Compose UI Test** y **Espresso** - pruebas unitarias e instrumentadas

## Arquitectura y Privacidad

Esta aplicación almacena toda la información de forma local en el dispositivo, sin backend ni servidor externo. La decisión considera que la Ley N° 19.628, modificada por la Ley N° 21.719, clasifica los datos de salud como datos sensibles: mantenerlos en el dispositivo evita transmitirlos o almacenarlos en servidores externos. Esto no constituye una declaración de cumplimiento normativo. Las librerías de ML Kit agregan el permiso de internet al manifiesto. El escaneo y la lectura de texto se hacen en el dispositivo, y la aplicación no envía datos de salud a ningún servidor.

Seguridad actual: el PIN se guarda con PBKDF2 y sal dentro de EncryptedSharedPreferences (Android Keystore), con límite de intentos, y la app vuelve a pedir el acceso tras 30 segundos fuera. la base de datos Room esta cifrada con SQLCipher: su clave es aleatoria y se guarda cifrada con una clave del Android Keystore, que no sale del teléfono. Al actualizar desde una versión anterior, la base sin cifrar se convierte en cifrada sin perder los datos. Los datos quedan excluidos del respaldo en la nube y de transferencia en otro dispositivo, por lo que no pasan a un teléfono nuevo: si se cambia o si se pierde el teléfono no se pueden recuperar, y el aviso de privacidad lo advierte. La única información visible sin PIN es la de emergencia, y solo de los pacientes en que el dueño la activo.  

El codigo esta organizado por paquetes segun responsabilidad:
- `ui/auth` - pantallas de bloqueo, creación y validación de PIN, e información de emergencia sin PIN.
- `ui/perfil` - pantalla inicial para crear el perfil del titular.
- `ui/historial` - lista de pacientes y pantalla del paciente con barra inferior (Hoy, Historial, Ficha y Compartir). Cada sección (medicamentos, horas médicas, exámenes y presión arterial) tiene su lista, su detalle y su formulario en archivos separados, y comparten componentes en `ComponentesHistorial.kt`.
- `ui/components` - selectores reutilizables de fecha y hora, aviso de ajustes para los recordatorios y aviso de privacidad.
- `data` - entidades y DAOs de Room, el paso de la base sin cifrar a cifrada, y las reglas del paciente, de los datos de emergencia, y de la presion arterial.
- `security` - biometría, manejo del PIN cifrado, hash y limite de intentos del PIN, reglas de bloqueo y clave de la base de datos.
- `alarms` - programacion de recordatorios, receivers de notificaciones y revisión de los permisos que necesitan.
- `export` - generacion del PDF del historial.
- `util` - utilidades de formato y conversión de fechas, reglas de texto compartidas por los formularios, guardado de los documentos escaneados y lectura de texto con ML Kit.

## Modelo de datos

La base de datos está en la versión 9 y se guarda cifrada con SQLCipher. Un **Perfil** representa al titular del dispositivo y agrupa a sus **Pacientes** (el propio titular y/o personas a su cargo). Cada paciente guarda su ficha (fecha de nacimiento, sexo, estado civil, previsión y grupo sanguíneo) y, de forma opcional, sus datos para emergencias (alergias, enfermedades y un contacto), junto con la opción de mostrarlos sin PIN.
De cada paciente dependen sus **Medicamentos**, **Horas Médicas**, **Exámenes** y registros de **Presión Arterial**. Los recordatorios se guardan en la tabla **Alarma** (varias por medicamento, o una por hora médica) y las tomas en **ControlToma**. Un medicamento con recordatorios puede guardar cuántas pastillas quedan y cuántas se toman en cada toma.

Desde la versión 7 el esquema se exporta en `app/schemas` y los cambios de estructura se hacen con migraciones. La versión 8 agrega el conteo de pastillas y la 9 los datos para emergencias, ambas con migraciones automáticas de Room que conservan los datos existentes. El borrado destructivo queda limitado a las versiones anteriores a la 7.

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
- **feature/reconocimiento-texto** - lectura de recetas, cajas y presión arterial con reconocimiento de texto
- **fix/recordatorios-samsung** - alarmas exactas con la exención de batería, aviso de ajustes y reprogramacion sin perder recordatorios pendientes
- **fix/seguridad** - limite de intentos y hash con sal del PIN, nuevo bloqueo al volver a la app, sin respaldo de datos y menos datos de salud visibles fuera de la app
- **fix/pdf** - texto largo partido entre páginas, imágenes reducidas para no agotar la memoria, aviso de documento no disponible y fecha de generación
- **feature/diseno-simple** - diseño simplificado con barra inferior Hoy, Historial y Compartir, listas con detalle, formularios en pantalla aparte y tema propio
- **feature/pruebas-avance** - pruebas de integración y de extremo a extremo del control de tomas, de los exámenes y del PIN incorrecto
- **feature/ficha-paciente** - ficha del paciente con formulario para agregar y editar, pestaña Ficha y datos de la ficha en el PDF
- **feature/editar-registros** - edición de medicamentos, horas médicas, exámenes y presiones ya guardados
- **feature/docker** - compilación del APK con Docker
- **feature/ajustes-usabilidad** - signo + en nombre y dosis, bloqueo sin huella, aviso de recordatorios en Hoy y Medicamentos, y aviso de privacidad
- **feature/rediseno** - rediseño de las pantallas de bloqueo, creación e ingreso del PIN
- **feature/bd-cifrada** - cifrado de la base de datos con SQLCipher y paso de la base existente a cifrada
- **feature/acceso-emergencia** - datos para emergencias del paciente y pantalla de información de emergencia sin PIN
- **feature/emergencia-yo-primero** - el titular del teléfono aparece primero en la pantalla de emergencia
- **feature/icono-app** - ícono propio de la aplicación
- **feature/rediseno-pacientes** - avatar con la inicial del paciente, fecha en Hoy, botones de alto mínimo y accesibilidad en Crear perfil

## Como Correr el Proyecto

1. Clonar el repositorio
2. Usar la rama 'main' (version estable) o 'develop' (version en desarrollo)
3. Abrir el proyecto en Android studio
4. Sincronizar Gradle (opcion: Sync Now)
5. Ejecutar en emulador o dispositivo físico con Android 7.0 (API 24) o superior
6. Para compilar el APK sin Android Studio, con Docker instalado y desde la carpeta del proyecto:
```
docker compose up --build
```
El APK queda en la carpeta `salida`. La primera vez descarga la imagen de compilación de Android, de unos 21 GB.

## Estado Actual

- **Autenticación:** biometría y PIN de respaldo funcionando. Sin huella registrada, el PIN es la forma principal de entrar.
- **Perfil y pacientes:** en el primer inicio se crea el perfil del titular, con la opción de registrarse también como paciente, y antes de guardar el primer dato se muestra el aviso de privacidad. Cada paciente tiene una ficha (fecha de nacimiento, sexo, estado civil, previsión y grupo sanguíneo) que se agrega y edita en un formulario aparte, con la edad calculada. La lista muestra un avatar con la inicial de cada paciente y avisa si a su ficha le faltan datos. Desde la ficha se edita o elimina el paciente.
- **Diseño y navegación:** la pantalla de cada paciente abre en **Hoy**, con la fecha del día, las tomas pendientes, la próxima hora médica y el próximo examen agendado. **Historial** muestra cuatro tarjetas (medicamentos, horas médicas, exámenes y presión arterial) con un resumen de cada una. Al tocar una se abre su lista, y cada registro se abre con "Ver detalle". Los formularios se muestran en una pantalla aparte y conservan lo escrito al cambiar de pestaña. Eliminar solo está dentro del detalle y pide confirmación. **Ficha** muestra los datos del paciente y el acceso de emergencia. **Compartir** genera el PDF. El tema usa la fuente Inter, con modo claro y oscuro según el ajuste del teléfono. Los botones principales tienen un alto mínimo de 56 dp y crecen con la letra grande. La aplicación tiene ícono propio.
- **Historial médico:**  registro, edición y eliminación de medicamentos, horas médicas, exámenes y presiones por paciente. Al editar una hora médica su recordatorio se rehace solo si cambia la fecha u hora, y al editar un medicamento solo cambian las alarmas de las horas modificadas. Medicamentos y exámenes permiten escanear y adjuntar documentos con ML Kit Document Scanner. Los documentos escaneados se copian al almacenamiento interno de la aplicación, para que no se pierdan si se borra el caché, y se eliminan junto con el medicamento, examen o paciente al que pertenecen. Las fechas y horas se eligen con calendario y reloj.
- **Solicitudes de exámenes:** al registrar un examen se indica si es próximo o ya fue realizado. Un examen próximo puede quedar sin fecha (solicitado) o con una fecha que no sea pasada (agendado); uno realizado exige una fecha que no sea futura y admite un resultado opcional. Un examen próximo se puede marcar como realizado desde su detalle. Opcionalmente se indica la consulta en que se pidió, elegida entre las horas medicas ya ocurridas del paciente; la fecha del examen no puede ser anterior a esa consulta, y la consulta se muestra en el detalle y en el PDF. Las reglas estan en `data/ReglasExamen.kt`.
- **Presión arterial:** registro de mediciones por paciente (sistólica, diastólica, pulso opcional, fecha y hora), con validación de rangos. La aplicación registra y muestra valores, no los interpreta.
- **Reconocimiento de texto:** al escanear una receta o la caja de un medicamento, la aplicación propone el nombre y la dosis, y en la caja también las pastillas, solo en los campos vacíos. La presión arterial se lee desde un papel, en números o palabras ("120/80" o "ciento veinte sobre ochenta") o con etiquetas en la misma línea. El texto se procesa en el dispositivo y el usuario revisa los valores antes de guardar. La receta queda adjunta; las fotos de la caja y de la presión no se guardan. No reconoce de forma confiable la letra manuscrita y, en las pruebas con un tensiómetro, no leyó correctamente su pantalla de 7 segmentos, por lo que esos valores se ingresan a mano. Las reglas están en `util/ReglasOcr.kt`.
- **Control de tomas:** la pestaña Hoy muestra cada toma del día de los medicamentos con recordatorio, primero las pendientes y en orden de hora, con el botón "Marcar como tomada". Una toma marcada queda como "Tomada" y se puede desmarcar si fue un error. Las marcas se guardan por día: al cambiar el día, las tomas vuelven a quedar pendientes.
- **Recuento de pastillas:** al agregar un medicamento con recordatorios se puede indicar cuántas pastillas quedan y cuántas se toman en cada toma. Marcar una toma descuenta esas pastillas y desmarcarla las devuelve, y tocar el botón varias veces seguidas no descuenta de más. La tarjeta y el detalle del medicamento muestran para cuántos días alcanzan y avisan "Reponga pronto" cuando alcanzan para 5 días o menos, y el mismo aviso se agrega a la notificación del recordatorio. En el detalle, el botón Reponer suma las pastillas compradas ("Contar pastillas" si el medicamento no llevaba la cuenta). Limitación conocida: si quedan menos pastillas que las de una toma, al desmarcarla se devuelven todas las de la toma. Las reglas están en `data/ReglasPastillas.kt`.
- **Alarmas y recordatorios:** varios recordatorios diarios por medicamento y un recordatorio por hora médica, eligiendo con cuánta anticipación avisar (30 minutos, 1 hora, 2 horas o 1 día). Con la exención del ahorro de batería, los recordatorios se programan con alarmas exactas. Sin ella son aproximados: Android puede retrasarlos hasta una hora y, en algunos fabricantes como Samsung, pueden quedar retenidos hasta abrir la aplicación. Si hay recordatorios activos y falta la exención o el permiso de notificaciones, la lista de pacientes muestra un aviso que lleva a cada ajuste. Al abrir la aplicación solo se programan las alarmas que faltan, para no perder un recordatorio pendiente; al reiniciar el dispositivo o actualizar la aplicación se reprograman todas.
- **Validación de entradas:** los campos de texto libre (nombres, relación, medicamento, dosis, especialidad, lugar, tipo y resultado de examen) quitan los espacios sobrantes, tienen un largo máximo con contador y aceptan solo los caracteres que corresponden a cada campo (por ejemplo, el nombre de una persona solo admite letras, espacios, apostrofo y guion). Las reglas están en `util/ReglasTexto.kt` y las usan todos los formularios.
- **Base de datos cifrada:** toda la información se guarda cifrada con SQLCipher. La clave se genera al azar en el primer inicio y se guarda cifrada con una clave del Android Keystore, por lo que las alarmas pueden leer la base sin que el usuario ingrese su PIN. Al actualizar desde una versión anterior, la base sin cifrar se copia a una cifrada y se reemplaza, y si la aplicación se cierra a mitad del proceso se retoma al volver a abrirla.
- **Acceso de emergencia:** cada paciente puede tener alergias, enfermedades y un contacto de emergencia. Si el dueño lo activa en la Ficha, la pantalla de bloqueo muestra "Información de emergencia", que sin PIN deja ver solo esos pacientes: grupo sanguíneo, previsión, alergias, enfermedades, medicamentos y el contacto, con un botón que abre el marcador con su número. El titular aparece primero. Editar al paciente no desactiva su información de emergencia.
- **Exportación:** desde la pestaña Compartir, el botón "Compartir historial en PDF" genera un PDF con la ficha y el historial completo del paciente (medicamentos, horas médicas, exámenes y presión arterial, incluidas las imágenes escaneadas) y lo comparte mediante el selector de apps del sistema. El PDF indica la fecha en que se generó, parte el texto largo entre páginas, reduce las imágenes para no agotar la memoria y muestra "(Documento no disponible)" si falta un archivo. El PDF temporal se borra al generar otro y al volver a abrir la aplicación.
- **CI/CD:** GitHub Actions compila el proyecto y ejecuta las pruebas unitarias en cada push.
- **Pruebas:**
  - 120 casos unitarios (conversión y formato de fechas, reglas de las alarmas, reglas del PIN, tiempo de bloqueo, intentos y hash del PIN, reglas del estado y la fecha de los exámenes, reglas de recuento de pastillas, reglas de la ficha del paciente y de los datos de emergencia, lectura de medicamentos y presión desde el texto escaneado, y reglas de texto con entradas inválidas: vacías, solo espacios, demasiado largas, con números, emojis o símbolos), que el CI ejecuta en cada push.
  - 22 casos de integración sobre la base de datos (creación del perfil, medicamentos con alarmas, borrado en cascada de un paciente, una toma marcada dos veces que descuenta una sola pastilla, un examen que se conserva al borrar su hora médica y los pacientes que se muestran en emergencia), sobre el paso de la base sin cifrar a cifrada (incluido un cierre a mitad del proceso), sobre la clave de la base de datos y sobre el PIN cifrado.
  - 5 pruebas de extremo a extremo que entran con PIN: exportar el PDF de un paciente, registrar una presión arterial, rechazar un PIN incorrecto, marcar una toma en Hoy que descuenta una pastilla, y activar la información de emergencia en la ficha para verla sin PIN desde el bloqueo.
  - Las de integración y extremo a extremo se ejecutan con Gradle en emuladores; el CI no las ejecuta.
  - Las de extremo a extremo borran los datos de la aplicación en el dispositivo donde se ejecutan.

## En Desarrollo y Pendiente
- **Seguridad:** cambio del PIN
- **Registros:** suspender un medicamento sin borrarlo
- **Recuento de pastillas:** guardar cuántas pastillas se descontaron en cada toma, para devolver solo esas al desmarcarla
- **Cambio de teléfono:** un respaldo cifrado con contraseña para pasar los datos a otro dispositivo
- **Inicio en modo oscuro:** la pantalla de carga usa el tema claro aunque el teléfono esté en modo oscuro
  
## Licencia

Este proyecto usa licencia MIT, ver archivo [LICENSE](LICENSE)
