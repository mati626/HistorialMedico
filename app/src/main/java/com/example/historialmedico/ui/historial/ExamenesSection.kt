package com.example.historialmedico.ui.historial

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.historialmedico.data.EstadoExamen
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.ReglasExamen
import com.example.historialmedico.ui.components.SelectorFechaDialog
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ExamenesSection(dao: ExamenDao, horaMedicaDao: HoraMedicaDao, paciente: Paciente) {
    val examenes by remember(paciente.id) { dao.getByPaciente(paciente.id) }.collectAsState(initial = emptyList())
    var tipo by remember { mutableStateOf("") }
    var realizado by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf<String?>(null) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var examenAEliminar by remember { mutableStateOf<Examen?>(null) }
    var examenAMarcar by remember { mutableStateOf<Examen?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as Activity
    var documentoUri by remember { mutableStateOf<String?>(null) }
    val horasMedicas by remember(paciente.id) {
        horaMedicaDao.getByPaciente(paciente.id)
    }.collectAsState(initial = emptyList())
    val consultasPorId = horasMedicas.associateBy { it.id }
    var consulta by remember { mutableStateOf<HoraMedica?>(null) }
    var menuConsultaAbierto by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val escaneo = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val paginas = escaneo?.pages
            if (!paginas.isNullOrEmpty()) {
                documentoUri = paginas[0].imageUri.toString()
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
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Examenes de ${paciente.nombre}",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        examenes.forEach { examen ->
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
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${examen.tipo} - ${examen.estado.etiqueta}")
                        val detalle = listOfNotNull(
                            examen.fecha?.let { Fechas.isoAVisible(it) },
                            examen.resultado
                        ).joinToString(" - ")
                        if (detalle.isNotBlank()) {
                            Text(
                                detalle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        examen.horaMedicaId?.let { consultasPorId[it] }?.let { consultaExamen ->
                            Text(
                                "Solicitado en: ${ReglasExamen.textoConsulta(consultaExamen)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (examen.estado != EstadoExamen.REALIZADO) {
                            TextButton(onClick = { examenAMarcar = examen }) {
                                Text("Marcar como realizado")
                            }
                        }
                    }
                    if (examen.documentoUri != null) {
                        TextButton(onClick = {
                            val archivo = File(Uri.parse(examen.documentoUri).path!!)
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                archivo
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Ver")
                        }
                    }
                    IconButton(onClick = { examenAEliminar = examen }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar examen",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        examenAEliminar?.let { examen ->
            AlertDialog(
                onDismissRequest = { examenAEliminar = null },
                title = { Text("Eliminar examen") },
                text = { Text("Seguro que quiere eliminar este examen?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(examen) }
                        examenAEliminar = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { examenAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        examenAMarcar?.let { examen ->
            MarcarRealizadoDialog(
                examen = examen,
                fechaConsulta = examen.horaMedicaId?.let { consultasPorId[it] }
                    ?.let { Fechas.millisAIso(it.fechaHora) },
                onConfirmar = { fechaRealizado, resultadoRealizado ->
                    scope.launch {
                        dao.update(
                            examen.copy(
                                estado = EstadoExamen.REALIZADO,
                                fecha = fechaRealizado,
                                resultado = resultadoRealizado
                            )
                        )
                    }
                    examenAMarcar = null
                },
                onCancelar = { examenAMarcar = null }
            )
        }

        OutlinedTextField(
            value = tipo,
            onValueChange = { if (it.length <= CampoTexto.TIPO_EXAMEN.maximo) tipo = it },
            label = { Text("Tipo de Examen") },
            supportingText = { Text("${tipo.length}/${CampoTexto.TIPO_EXAMEN.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(
                onClick = { menuConsultaAbierto = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solicitado en: ${consulta?.let { ReglasExamen.textoConsulta(it) } ?: "sin consulta"}")
            }
            DropdownMenu(
                expanded = menuConsultaAbierto,
                onDismissRequest = { menuConsultaAbierto = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin consulta") },
                    onClick = {
                        consulta = null
                        menuConsultaAbierto = false
                    }
                )
                ReglasExamen.consultasPosibles(horasMedicas, System.currentTimeMillis())
                    .forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(ReglasExamen.textoConsulta(opcion)) },
                            onClick = {
                                consulta = opcion
                                menuConsultaAbierto = false
                            }
                        )
                    }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !realizado,
                onClick = {
                    realizado = false
                    resultado = ""
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Próximo")
            }
            SegmentedButton(
                selected = realizado,
                onClick = { realizado = true },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Realizado")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { mostrarSelectorFecha = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(fecha?.let { "Fecha: ${Fechas.isoAVisible(it)}" }
                ?: if (realizado) "Elegir fecha en que se realizó" else "Elegir fecha (opcional)")
        }
        if (fecha != null) {
            TextButton(onClick = { fecha = null }) {
                Text("Quitar fecha")
            }
        }
        if (mostrarSelectorFecha) {
            SelectorFechaDialog(
                onConfirmar = {
                    fecha = Fechas.utcMillisAIso(it)
                    mostrarSelectorFecha = false
                },
                onCancelar = { mostrarSelectorFecha = false }
            )
        }
        if (realizado) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = resultado,
                onValueChange = { if (it.length <= CampoTexto.RESULTADO.maximo) resultado = it },
                label = { Text("Resultado (opcional)") },
                supportingText = { Text("${resultado.length}/${CampoTexto.RESULTADO.maximo}") },
                modifier = Modifier.fillMaxWidth()
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
            Text(if (documentoUri == null) "Escanear Documento" else "Documento escaneado (volver a escanear)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val errorTexto = ReglasTexto.validar(tipo, CampoTexto.TIPO_EXAMEN)
                ?: ReglasTexto.validar(resultado, CampoTexto.RESULTADO, obligatorio = false)
                ?: ReglasExamen.validarFecha(realizado, fecha, Fechas.hoyIso())
                ?: ReglasExamen.validarConsulta(
                    fecha,
                    consulta?.let { Fechas.millisAIso(it.fechaHora) })
            when {
                errorTexto != null -> error = errorTexto
                else -> {
                    val tipoAGuardar = ReglasTexto.normalizar(tipo)
                    val fechaAGuardar = fecha
                    val estadoAGuardar = ReglasExamen.estado(realizado, fechaAGuardar)
                    val resultadoAGuardar = ReglasTexto.normalizar(resultado).ifBlank { null }
                    val documentoAGuardar = documentoUri
                    val consultaAGuardar = consulta?.id
                    tipo = ""
                    realizado = false
                    fecha = null
                    resultado = ""
                    documentoUri = null
                    consulta = null
                    error = null
                    scope.launch {
                        dao.insert(
                            Examen(
                                pacienteId = paciente.id,
                                tipo = tipoAGuardar,
                                fecha = fechaAGuardar,
                                resultado = resultadoAGuardar,
                                estado = estadoAGuardar,
                                documentoUri = documentoAGuardar,
                                horaMedicaId = consultaAGuardar
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
            Text(
                it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MarcarRealizadoDialog(
    examen: Examen,
    fechaConsulta: String?,
    onConfirmar: (fecha: String, resultado: String?) -> Unit,
    onCancelar: () -> Unit
) {
    var fecha by remember {
        mutableStateOf(examen.fecha?.takeIf {
            ReglasExamen.validarFecha(true, it, Fechas.hoyIso()) == null
        })
    }
    var resultado by remember { mutableStateOf("") }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Marcar como realizado") },
        text = {
            Column {
                Text(examen.tipo)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { mostrarSelectorFecha = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(fecha?.let { "Fecha: ${Fechas.isoAVisible(it)}" }
                        ?: "Elegir fecha en que se realizó")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = resultado,
                    onValueChange = {
                        if (it.length <= CampoTexto.RESULTADO.maximo) resultado = it
                    },
                    label = { Text("Resultado (opcional)") },
                    supportingText = {
                        Text("${resultado.length}/${CampoTexto.RESULTADO.maximo}")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(
                        it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val fechaElegida = fecha
                val errorEncontrado = ReglasExamen.validarFecha(
                    true,
                    fechaElegida, Fechas.hoyIso()
                )
                    ?: ReglasExamen.validarConsulta(fechaElegida, fechaConsulta)
                    ?: ReglasTexto.validar(
                        resultado,
                        CampoTexto.RESULTADO, obligatorio = false
                    )
                when {
                    errorEncontrado != null -> error = errorEncontrado
                    fechaElegida != null -> onConfirmar(
                        fechaElegida,
                        ReglasTexto.normalizar(resultado).ifBlank { null })
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
    if (mostrarSelectorFecha) {
        SelectorFechaDialog(
            onConfirmar = {
                fecha = Fechas.utcMillisAIso(it)
                mostrarSelectorFecha = false
            },
            onCancelar = { mostrarSelectorFecha = false }
        )
    }
}

