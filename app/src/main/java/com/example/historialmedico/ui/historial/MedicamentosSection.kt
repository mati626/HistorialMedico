package com.example.historialmedico.ui.historial

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.ControlToma
import com.example.historialmedico.data.ControlTomaDao
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.ui.components.SelectorHoraDialog
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun MedicamentosSection(
    dao: MedicamentoDao,
    alarmaDao: AlarmaDao,
    controlTomaDao: ControlTomaDao,
    paciente: Paciente
) {
    val medicamentos by remember(paciente.id) { dao.getByPaciente(paciente.id) }.collectAsState(
        initial = emptyList()
    )
    val alarmas by remember(paciente.id) { alarmaDao.getDeMedicamentos(paciente.id) }.collectAsState(
        initial = emptyList()
    )
    val alarmasPorMedicamento = alarmas.groupBy { it.medicamentoId }
    var hoy by remember { mutableStateOf(Fechas.hoyIso()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hoy = Fechas.hoyIso() }
    val tomas by remember(paciente.id, hoy) {
        controlTomaDao.getDelDia(
            paciente.id,
            hoy
        )
    }.collectAsState(initial = emptyList())
    var medicamentoAEliminar by remember { mutableStateOf<Medicamento?>(null) }
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    val horas = remember { mutableStateListOf<Int>() }
    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun guardarToma(medicamentoId: Long, hora: Int, tomado: Boolean) {
        scope.launch {
            controlTomaDao.guardar(
                ControlToma(
                    medicamentoId = medicamentoId,
                    fecha = hoy,
                    hora = hora,
                    tomado = tomado
                )
            )
        }
    }

    val context = LocalContext.current
    val appContext = context.applicationContext
    val activity = context as Activity
    var documentoUri by remember { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val resultado = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val paginas = resultado?.pages
            if (!paginas.isNullOrEmpty()) {
                val escaneado = paginas[0].imageUri.toString()
                scope.launch {
                    val guardado = Documentos.guardar(context, escaneado)
                    if (guardado == null) {
                        error = "No se pudo guardar la receta escaneada"
                    } else {
                        documentoUri?.let { Documentos.borrar(it) }
                        documentoUri = guardado
                    }
                }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Medicamentos de ${paciente.nombre}", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        medicamentos.forEach { medicamento ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    if (medicamento.documentoUri != null) {
                        TextButton(onClick = {
                            val archivo = File(Uri.parse(medicamento.documentoUri).path!!)
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider", archivo
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Ver Receta")
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${medicamento.nombre} - ${medicamento.dosis}")
                        val alarmasDelMedicamento =
                            alarmasPorMedicamento[medicamento.id].orEmpty()
                        if (alarmasDelMedicamento.isNotEmpty()) {
                            val horasDelMedicamento = alarmasDelMedicamento.mapNotNull { it.hora }
                            val horasTomadas = tomas
                                .filter { it.medicamentoId == medicamento.id && it.tomado }
                                .map { it.hora }
                            val tomadasHoy = horasDelMedicamento.count { it in horasTomadas }
                            Text(
                                "Tomas de hoy: $tomadasHoy de ${horasDelMedicamento.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            horasDelMedicamento.forEach { hora ->
                                val tomado = hora in horasTomadas
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .toggleable(
                                            value = tomado,
                                            role = Role.Checkbox,
                                            onValueChange = { marcado ->
                                                guardarToma(
                                                    medicamento.id,
                                                    hora,
                                                    marcado
                                                )
                                            }
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = tomado, onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(Fechas.minutosAHora(hora))
                                }
                            }
                        }
                    }
                    IconButton(onClick = { medicamentoAEliminar = medicamento }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar medicamento",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
                        alarmasPorMedicamento[medicamento.id].orEmpty().forEach { alarma ->
                            AlarmScheduler.cancelar(appContext, alarma.id)
                        }
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
                onValueChange = {
                    if (it.length <= CampoTexto.MEDICAMENTO.maximo) nombre = it
                },
                label = { Text("Nombre") },
                supportingText = {
                    Text("${nombre.length}/${CampoTexto.MEDICAMENTO.maximo}")
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = dosis,
                onValueChange = {
                    if (it.length <= CampoTexto.DOSIS.maximo) dosis = it
                },
                label = { Text("Dosis") },
                supportingText = {
                    Text("${dosis.length}/${CampoTexto.DOSIS.maximo}")
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Recordatorios diarios (opcional)",
            style = MaterialTheme.typography.bodyMedium
        )
        horas.sorted().forEach { hora ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Fechas.minutosAHora(hora), modifier = Modifier.weight(1f))
                TextButton(onClick = { horas.remove(hora) }) {
                    Text("Quitar")
                }
            }
        }
        OutlinedButton(onClick = { mostrarSelectorHora = true }) {
            Text("Agregar hora")
        }
        if (mostrarSelectorHora) {
            SelectorHoraDialog(
                onConfirmar = { hora, minuto ->
                    val minutos = hora * 60 + minuto
                    if (minutos !in horas) horas.add(minutos)
                    mostrarSelectorHora = false
                },
                onCancelar = { mostrarSelectorHora = false }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher
                        .launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener {
                    error = "No se logro abrir el escaner"
                }
        }) {
            Text(if (documentoUri == null) "Escanear Receta" else "Receta escaneada (volver a escanear)")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val nombreAGuardar = ReglasTexto.normalizar(nombre)
            val dosisAGuardar = ReglasTexto.normalizar(dosis)
            val errorTexto = ReglasTexto.validar(nombre, CampoTexto.MEDICAMENTO)
                ?: ReglasTexto.validar(dosis, CampoTexto.DOSIS)
            when {
                errorTexto != null -> error = errorTexto
                !dosis.any { it.isDigit() } -> error =
                    "La dosis debe incluir un numero (ej: 500 mg)"

                medicamentos.size >= MAX_MEDICAMENTOS_POR_PACIENTE -> error =
                    "Este paciente ya tiene el maximo de $MAX_MEDICAMENTOS_POR_PACIENTE medicamentos"

                medicamentos.any {
                    ReglasTexto.normalizar(it.nombre).equals(nombreAGuardar, ignoreCase = true)
                } -> error = "Ya existe un medicamento con ese nombre para este paciente"

                else -> {
                    val medicamento = Medicamento(
                        pacienteId = paciente.id,
                        nombre = nombreAGuardar,
                        dosis = dosisAGuardar,
                        documentoUri = documentoUri
                    )

                    val horasAguardar = horas.toList()
                    nombre = ""
                    dosis = ""
                    horas.clear()
                    documentoUri = null
                    error = null
                    scope.launch {
                        withContext(NonCancellable) {
                            val alarmasCreadas = dao.insertConAlarmas(medicamento, horasAguardar)
                            alarmasCreadas.forEach {
                                AlarmScheduler.programar(appContext, it)
                            }
                        }
                    }
                }
            }
        }) {
            Text("Agregar Medicamento")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private const val MAX_MEDICAMENTOS_POR_PACIENTE = 10