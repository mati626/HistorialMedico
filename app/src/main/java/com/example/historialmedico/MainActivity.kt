package com.example.historialmedico

import java.io.File
import android.net.Uri
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.historialmedico.security.BiometricAuthManager
import com.example.historialmedico.security.PinManager
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import com.example.historialmedico.data.AppDataBase
import com.example.historialmedico.data.Perfil
import com.example.historialmedico.data.PerfilDao
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.ExamenDao
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

enum class AuthScreen {CREATE_PIN, LOCKED, ENTER_PIN, AUTHENTICATED}

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var pinManager: PinManager
    private lateinit var database: AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricAuthManager = BiometricAuthManager(this)
        pinManager = PinManager(this)
        database = AppDataBase.getInstance(this)

        setContent {
            HistorialMedicoTheme {
                var screen by remember { mutableStateOf(if (pinManager.hasPinSet()) AuthScreen.LOCKED else AuthScreen.CREATE_PIN) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screen) {
                        AuthScreen.AUTHENTICATED -> PerfilesScreen(
                            dao = database.perfilDao(),
                            medicamentoDao = database.medicamentoDao(),
                            horaMedicaDao = database.horaMedicaDao(),
                            examenDao = database.examenDao(),
                            modifier = Modifier.padding(innerPadding)
                        )
                        AuthScreen.LOCKED -> LockScreen(
                            modifier = Modifier.padding(innerPadding),
                            errorMessage = errorMessage,
                            showBiometricOption = biometricAuthManager.canAuthenticate(),
                            onUnlockClick = {
                                biometricAuthManager.authenticate(
                                    title = "Historial Medico",
                                    subtitle = "Ingrese sus datos para continuar",
                                    negativeButtonText = "Cancelar",
                                    onSuccess = {
                                        screen = AuthScreen.AUTHENTICATED
                                        errorMessage = null
                                    },
                                    onError = { _, errString -> errorMessage = errString.toString() },
                                    onFailed = { errorMessage = "No se reconoce la biometria, intente nuevamente " }
                                )
                            },
                            onUsePinClick = {
                                screen = AuthScreen.ENTER_PIN
                                errorMessage = null
                            }
                        )
                        AuthScreen.CREATE_PIN -> CreatePinScreen(
                            modifier = Modifier.padding(innerPadding),
                            onPinCreated = { pin ->
                                pinManager.setPin(pin)
                                screen = AuthScreen.LOCKED
                            }
                        )
                        AuthScreen.ENTER_PIN -> PinEntryScreen(
                            modifier = Modifier.padding(innerPadding),
                            errorMessage = errorMessage,
                            onPinEntered = { pin ->
                                if (pinManager.validatePin(pin)) {
                                    screen = AuthScreen.AUTHENTICATED
                                    errorMessage = null
                                } else {
                                    errorMessage = "PIN incorrecto"
                                }
                            },
                            onBack = {
                                screen = AuthScreen.LOCKED
                                errorMessage = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreen(
    modifier: Modifier=Modifier,
    errorMessage:String?,
    onUnlockClick:()-> Unit,
    showBiometricOption: Boolean,
    onUsePinClick:()-> Unit
){
    Column(modifier=modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(
            imageVector=Icons.Filled.Lock,
            contentDescription=null,
            modifier= Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier= Modifier.height(24.dp))

        Text(text="Aplicacion Bloqueada",
            style=MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )

        Text(
            text="Use su huella o rostro para acceder a su historial Medico",
            style = MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant,
            modifier= Modifier.padding(top=8.dp,bottom=32.dp)
        )

        if(showBiometricOption) {
            Button(
                onClick = onUnlockClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null, modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Desbloquear con biometria")
            }
        }
        TextButton(onClick = onUsePinClick) {Text("Usar Pin en su lugar") }
        errorMessage?.let{
            Spacer(modifier= Modifier.height(16.dp))
            Text(
                text=it,
                color= MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CreatePinScreen(
    modifier: Modifier= Modifier,
    onPinCreated:(String)->Unit
){
    var pin by remember { mutableStateOf("")}
    var confirmPin by remember {mutableStateOf("")}
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier=modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier= Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier= Modifier.height(16.dp))

        Text("Cree su PIN de Acceso", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text(
            "Su pin se usara si la biometria falla",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier= Modifier.padding(top=8.dp,bottom=24.dp)
        )
        OutlinedTextField(
            value = pin,
            onValueChange = {if (it.length<=6 && it.all(Char::isDigit))pin = it},
            label = {Text("PIN (4 a 6 digitos)")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier= Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { if (it.length <=6 && it.all(Char::isDigit)) confirmPin= it},
            label = {Text("Confirme su PIN")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType= KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(modifier= Modifier.height(12.dp))
            Text(it,color= MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier= Modifier.height(24.dp))

        Button(
            colors= ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            onClick = {
                when {
                    pin.length < 4 -> error = "El PIN debe tener minimo 4 digitos"
                    pin != confirmPin->error= "Los PIN no coinciden"
                    else -> onPinCreated(pin)
                }
            },
            modifier= Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Guardar PIN")
        }
    }
}
@Composable
fun PinEntryScreen(
    modifier: Modifier= Modifier,
    errorMessage: String?,
    onPinEntered: (String)->Unit,
    onBack:()-> Unit
){
    var pin by remember { mutableStateOf("") }

    Column(
        modifier=modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier=Modifier.size(64.dp),
            tint=MaterialTheme.colorScheme.primary
        )
        Spacer(modifier= Modifier.height(16.dp))
        Text("Ingrese su Pin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(24.dp))

        OutlinedTextField(
            value=pin,
            onValueChange={if (it.length <=6 && it.all(Char::isDigit))pin=it},
            label={Text("PIN")},
            visualTransformation= PasswordVisualTransformation(),
            keyboardOptions= KeyboardOptions(keyboardType =
                KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        errorMessage?.let{
            Spacer(modifier= Modifier.height(12.dp))
            Text(it,color=MaterialTheme.colorScheme.error,style= MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier= Modifier.height(24.dp))
        Button(
            colors= ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            onClick = {onPinEntered(pin)},
            modifier= Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Desbloquear")
        }
        TextButton(onClick = onBack,modifier= Modifier.padding(top=8.dp)) {
            Text("Volver")
        }
    }
}
@Composable
fun PerfilesScreen(dao: PerfilDao, medicamentoDao: MedicamentoDao,horaMedicaDao: HoraMedicaDao, examenDao: ExamenDao, modifier: Modifier=Modifier){
    val perfiles by dao.getAll().collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var perfilSeleccionado by remember { mutableStateOf<Perfil?>(null) }
    var perfilAEliminar by remember { mutableStateOf<Perfil?>(null) }



    Column(modifier=modifier
        .fillMaxSize()
        .imePadding()
        .padding(16.dp)) {
        Text("Perfiles", style= MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(16.dp))

        LazyColumn(modifier= Modifier.weight(1f)) {
            items(perfiles) { perfil ->
                Row(
                    modifier=Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            perfilSeleccionado = perfil
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${perfil.nombre} (${perfil.relacion})",
                        modifier= Modifier.weight(1f)
                    )
                    IconButton(onClick =  {
                        perfilAEliminar=perfil
                    }){
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar perfil")
                    }
                }
            }
        }
        Row(verticalAlignment= Alignment.CenterVertically){
            OutlinedTextField(
                value=nombre,
                onValueChange = {nombre= it},
                label = {Text("Nombre")},
                modifier= Modifier.weight(1f)
            )
            Spacer(modifier= Modifier.width(8.dp))
            Button(onClick={
                if(nombre.isNotBlank()){
                    scope.launch {
                        dao.insert(Perfil(nombre=nombre,relacion="Yo"))
                    }
                    nombre = ""
                }
            }) {
                Text("Agregar")
            }
        }
        perfilSeleccionado?.let { perfil ->
            Spacer(modifier=Modifier.height(24.dp))
            MedicamentosSection(dao=medicamentoDao,perfil=perfil)
            Spacer(modifier= Modifier.height(24.dp))
            HorasMedicasSection(dao=horaMedicaDao,perfil=perfil)
            Spacer(modifier= Modifier.height(24.dp))
            ExamenesSection(dao = examenDao,perfil=perfil)
        }
        perfilAEliminar?.let { perfil -> AlertDialog(
            onDismissRequest = {perfilAEliminar=null},
            title = {Text("Eliminar perfil")},
            text = {Text("Seguro que quieres eliminar a ${perfil.nombre}? Se eliminaran tambien todos sus medicamentos.")},
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { dao.delete(perfil) }
                    perfilAEliminar=null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {perfilAEliminar=null}) {
                    Text("Cancelar")
                }
            }
        ) }
    }
}

@Composable
fun MedicamentosSection (dao: MedicamentoDao,perfil: Perfil) {
    val medicamentos by dao.getByPerfil(perfil.id).collectAsState(initial = emptyList())
    var medicamentoAEliminar by remember{mutableStateOf<Medicamento?>(null)}
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(
            "Medicamentos de ${perfil.nombre}", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        medicamentos.forEach { medicamento ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (medicamento.dosis.isBlank()) medicamento.nombre
                    else "${medicamento.nombre}- ${medicamento.dosis}",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { medicamentoAEliminar=medicamento }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar medicamento"
                    )
                }
            }
        }
        medicamentoAEliminar?.let { medicamento ->
            AlertDialog(
                onDismissRequest = { medicamentoAEliminar = null },
                title = { Text("Eliminar Medicamento") },
                text = { Text("Seguro que quieres eliminar ${medicamento.nombre}?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(medicamento) }
                        medicamentoAEliminar = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        medicamentoAEliminar = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = dosis,
                onValueChange = { dosis = it },
                label = { Text("Dosis") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                nombre.isBlank()->error="El nombre del medicamento es obligatorio"
                dosis.isBlank()->error="La dosis es obligatoria"
                !dosis.any{it.isDigit()}->error="La dosis debe incluir un numero (ej:500mg)"
                medicamentos.size>=MAX_MEDICAMENTOS_POR_PERFIL->error="Este perfil ya tiene el maximo de $MAX_MEDICAMENTOS_POR_PERFIL medicamentos"
                medicamentos.any{it.nombre.trim().equals(nombre.trim(),ignoreCase = true)}->error="Ya existe un medicamento con ese nombre para este perfil"
                else->{
                    val nombreAGuardar=nombre.trim()
                    val dosisAGuardar=dosis.trim()
                    nombre=""
                    dosis=""
                    error=null
                    scope.launch {
                        dao.insert(
                            Medicamento(
                                perfilId = perfil.id,
                                nombre = nombreAGuardar,
                                dosis=dosisAGuardar
                            )
                        )
                    }
                }
            }
        }) {
            Text("Agregar Medicamento")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable
fun HorasMedicasSection(dao: HoraMedicaDao, perfil: Perfil){
    val horas by dao.getByPerfil(perfil.id).collectAsState(initial =
        emptyList())
    var especialidad by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var horaAEliminar by remember { mutableStateOf<HoraMedica?>(null) }
    var scope = rememberCoroutineScope()

    Column(modifier =
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            "Horas Medicas de ${perfil.nombre}", style =
                MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        horas.forEach { hora ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    "${hora.especialidad}-${hora.fecha} (${hora.lugar})",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {horaAEliminar=hora}) {
                    Icon(imageVector=Icons.Filled.Delete,
                        contentDescription = "Eliminar hora medica")
                }
            }
        }

        horaAEliminar?.let { hora->
            AlertDialog(
                onDismissRequest = {horaAEliminar=null},
                title = {Text("Eliminar hora medica")},
                text = {Text("Seguro que quiere eliminar esta hora medica?")},
                confirmButton={
                    TextButton(onClick = {
                        scope.launch { dao.delete(hora) }
                        horaAEliminar=null
                    }){
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {horaAEliminar=null}){
                        Text("Cancelar")
                    }
                }
            )
        }
        OutlinedTextField(
            value = especialidad,
            onValueChange = {especialidad=it},
            label = {Text("Especialidad")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fecha,
            onValueChange = {fecha=it},
            label = {Text("Fecha (ej: 18/09/2026)")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lugar,
            onValueChange = {lugar=it},
            label = {Text("Lugar")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick ={
            when {
                especialidad.isBlank() -> error = "La especialidad es obligatoria"
                fecha.isBlank() -> error = "La fecha es obligatoria"
                lugar.isBlank() -> error = "El lugar es obligatorio"
                else -> {
                    val especialidadAGuardar = especialidad.trim()
                    val fechaAGuardar = fecha.trim()
                    val lugarAGuardar = lugar.trim()
                    especialidad = ""
                    fecha = ""
                    lugar = ""
                    error = null
                    scope.launch {
                        dao.insert(
                            HoraMedica(
                                perfilId = perfil.id,
                                especialidad = especialidadAGuardar,
                                fecha = fechaAGuardar,
                                lugar = lugarAGuardar
                            )
                        )
                    }
                }
            }
        }){
            Text("Agregar Hora Medica")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error, style=MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ExamenesSection(dao: ExamenDao,perfil: Perfil){
    val examenes by dao.getByPerfil(perfil.id).collectAsState(initial = emptyList())
    var tipo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var examenAEliminar by remember { mutableStateOf<Examen?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as Activity
    var documentoUri by remember { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode== Activity.RESULT_OK){
            val resultado = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val paginas = resultado?.pages
            if(!paginas.isNullOrEmpty()){
                documentoUri=paginas[0].imageUri.toString()
            }
        }
    }

    val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()

    val scanner = GmsDocumentScanning.getClient(scannerOptions)



    Column(modifier =
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            "Examenes de ${perfil.nombre}",
            style=MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        examenes.forEach { examen ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    "${examen.tipo} - ${examen.fecha} (${examen.resultado})",
                    modifier = Modifier.weight(1f)
                )
                if(examen.documentoUri!=null){
                    TextButton(onClick = {
                        val archivo= File(Uri.parse(examen.documentoUri).path!!)
                        val contentUri= FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",archivo)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(contentUri,"image/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Ver")
                    }
                }
                IconButton(onClick = {examenAEliminar=examen}) {
                    Icon(imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar examen")
                }
            }
        }
        examenAEliminar?.let { examen ->
            AlertDialog(
                onDismissRequest = {examenAEliminar=null},
                title = {Text("Eliminar examen")},
                text = {Text("Seguro que quiere eliminar este examen?")},
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(examen) }
                        examenAEliminar=null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton={
                    TextButton(onClick = {examenAEliminar=null}) {
                        Text("Cancelar")
                    }
                }
            )
        }

        OutlinedTextField(
            value=tipo,
            onValueChange = {tipo=it},
            label = {Text("Tipo de Examen")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value=fecha,
            onValueChange = {fecha=it},
            label = {Text("Fecha (ej: 18/09/2026)")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = resultado,
            onValueChange = {resultado=it},
            label = {Text("Resultado")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender -> scannerLauncher
                    .launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener {
                    error="No se logro abrir el escaner"
                }
        }) {
            Text(if(documentoUri==null)"Escanear Documento" else "Documento escaneado (volver a escanear)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                tipo.isBlank()->error="El tipo de examen es obligatorio"
                fecha.isBlank()->error="La fecha es obligatoria"
                resultado.isBlank()->error="El resultado es obligatorio"
                else->{
                    val tipoAGuardar = tipo.trim()
                    val fechaAGuardar = fecha.trim()
                    val resultadoAGuardar = resultado.trim()
                    val documentoAGuardar = documentoUri
                    tipo=""
                    fecha=""
                    resultado=""
                    documentoUri=null
                    error=null
                    scope.launch {
                        dao.insert(
                            Examen(
                                perfilId = perfil.id,
                                tipo = tipoAGuardar,
                                fecha = fechaAGuardar,
                                resultado = resultadoAGuardar,
                                documentoUri = documentoAGuardar
                            )
                        )
                    }
                }
            }
        }) {
            Text("Agregar Examen")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

private const val MAX_MEDICAMENTOS_POR_PERFIL=10