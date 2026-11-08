package com.example.historialmedico.ui.historial

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.ReglasPastillas
import com.example.historialmedico.ui.components.SelectorHoraDialog
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.LectorTexto
import com.example.historialmedico.util.ReglasOcr
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//formulario en su propia pantalla, guardar lo hace la lista que sigue abierta al cerrarlo
@Composable
fun MedicamentoFormulario(
    paciente: Paciente,
    existentes: List<Medicamento>,
    onGuardar: (medicamento: Medicamento, horas: List<Int>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    //rememberSaveable: lo escrito se mantiene al cambiar el tema o al volver del escaner
    var nombre by rememberSaveable { mutableStateOf("") }
    var dosis by rememberSaveable { mutableStateOf("") }
    var horas by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var pastillas by rememberSaveable { mutableStateOf("") }
    var pastillasPorToma by rememberSaveable { mutableStateOf("1") }
    var documentoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarSelectorHora by remember { mutableStateOf(false) }
    //evita guardar dos veces si se toca Guardar dos veces seguidas
    var guardado by remember { mutableStateOf(false) }

    //prellena con lo leido solo en los campos vacios, no pisa lo que el usuario escribio
    suspend fun prellenarDesde(uri: Uri, conCantidad: Boolean) {
        aviso = "Leyendo el texto..."
        val leido = LectorTexto.leer(context, uri)?.let { ReglasOcr.leerMedicamento(it) }
        if (leido == null) {
            aviso = "No se pudo leer la imagen; complete los datos a mano"
            return
        }
        val cantidad = if (conCantidad) leido.cantidad else null
        if (nombre.isBlank()) leido.nombre?.let { nombre = it }
        if (dosis.isBlank()) leido.dosis?.let { dosis = it }
        if (pastillas.isBlank()) cantidad?.let { pastillas = it.toString() }
        aviso = when {
            leido.nombre == null && leido.dosis == null && cantidad == null ->
                "No se encontraron datos; complételos a mano"

            cantidad != null && horas.isEmpty() ->
                "Revise los datos; las pastillas aparecen al agregar un recordatorio"

            else -> "Revise los datos leídos antes de guardar"
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val resultado = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val paginas = resultado?.pages
            if (!paginas.isNullOrEmpty()) {
                val escaneado = paginas[0].imageUri.toString()
                scope.launch {
                    val nuevo = Documentos.guardar(context, escaneado)
                    if (nuevo == null) {
                        error = "No se pudo guardar la receta escaneada"
                    } else {
                        documentoUri?.let { Documentos.borrar(it) }
                        documentoUri = nuevo
                        prellenarDesde(Uri.parse(nuevo), conCantidad = false)
                    }
                }
            }
        }
    }

    //la caja solo se lee para prellenar: no se adjunta y su foto se borra del cache
    val cajaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val pagina = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
                ?.pages?.firstOrNull()
            if (pagina != null) {
                scope.launch {
                    try {
                        prellenarDesde(pagina.imageUri, conCantidad = true)
                    } finally {
                        withContext(NonCancellable) { Documentos.borrar(pagina.imageUri.toString()) }
                    }
                }
            }
        }
    }

    val scanner = remember { Documentos.crearEscaner() }

    fun guardar() {
        if (guardado) return
        val nombreAGuardar = ReglasTexto.normalizar(nombre)
        val dosisAGuardar = ReglasTexto.normalizar(dosis)
        //las pastillas solo se piden con recordatorios, porque solo esas tomas se marcan en Hoy
        val errorPastillas = when {
            horas.isEmpty() -> null
            pastillas.isBlank() -> ReglasPastillas.validarUnidadesPorToma(pastillasPorToma.toIntOrNull())
            else -> ReglasPastillas.validarCantidad(pastillas.toIntOrNull())
                ?: ReglasPastillas.validarUnidadesPorToma(pastillasPorToma.toIntOrNull())
        }
        error = ReglasTexto.validar(nombre, CampoTexto.MEDICAMENTO)
            ?: ReglasTexto.validar(dosis, CampoTexto.DOSIS)
                    ?: errorPastillas
                    ?: when {
                !dosis.any { it.isDigit() } -> "La dosis debe incluir un número (ej: 500 mg)"

                existentes.size >= MAX_MEDICAMENTOS_POR_PACIENTE ->
                    "Este paciente ya tiene el máximo de $MAX_MEDICAMENTOS_POR_PACIENTE medicamentos"

                existentes.any {
                    ReglasTexto.normalizar(it.nombre).equals(nombreAGuardar, ignoreCase = true)
                } -> "Ya existe un medicamento con ese nombre para este paciente"

                else -> null
            }
        if (error != null) return
        guardado = true
        onGuardar(
            Medicamento(
                pacienteId = paciente.id,
                nombre = nombreAGuardar,
                dosis = dosisAGuardar,
                documentoUri = documentoUri,
                cantidad = if (horas.isEmpty()) null else pastillas.toIntOrNull(),
                unidadesPorToma = if (horas.isEmpty()) 1 else pastillasPorToma.toIntOrNull() ?: 1
            ),
            horas
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Puede escanear la receta o la caja para completar los datos más rápido.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = {
                Documentos.abrirEscaner(context, scanner, scannerLauncher) {
                    error = "No se pudo abrir el escáner"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(if (documentoUri == null) "Escanear receta" else "Receta escaneada (volver a escanear)")
        }
        OutlinedButton(
            onClick = {
                Documentos.abrirEscaner(context, scanner, cajaLauncher) {
                    error = "No se pudo abrir el escáner"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Escanear caja")
        }
        aviso?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = nombre,
            onValueChange = { if (it.length <= CampoTexto.MEDICAMENTO.maximo) nombre = it },
            label = { Text("Nombre") },
            supportingText = { Text("${nombre.length}/${CampoTexto.MEDICAMENTO.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = dosis,
            onValueChange = { if (it.length <= CampoTexto.DOSIS.maximo) dosis = it },
            label = { Text("Dosis") },
            supportingText = {
                Row {
                    Text("Ej: 500 mg", modifier = Modifier.weight(1f))
                    Text("${dosis.length}/${CampoTexto.DOSIS.maximo}")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Recordatorios diarios (opcional)", style = MaterialTheme.typography.titleSmall)
        horas.forEach { hora ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    Fechas.minutosAHora(hora),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { horas = horas - hora }) { Text("Quitar") }
            }
        }
        OutlinedButton(
            onClick = { mostrarSelectorHora = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar hora")
        }
        if (horas.isNotEmpty()) {
            OutlinedTextField(
                value = pastillas,
                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) pastillas = it },
                label = { Text("Pastillas que tiene (opcional)") },
                supportingText = {
                    Text("Avisa cuando alcancen para ${ReglasPastillas.DIAS_AVISO} días o menos")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pastillasPorToma,
                onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) pastillasPorToma = it
                },
                label = { Text("Pastillas por toma") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        //el error va justo sobre el boton, donde se esta mirando al tocar Guardar
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = { guardar() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text("Guardar medicamento")
        }
    }

    if (mostrarSelectorHora) {
        SelectorHoraDialog(
            onConfirmar = { hora, minuto ->
                val minutos = hora * 60 + minuto
                if (minutos !in horas) horas = (horas + minutos).sorted()
                mostrarSelectorHora = false
            },
            onCancelar = { mostrarSelectorHora = false }
        )
    }
}