package com.example.historialmedico.ui.historial

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.ReglasExamen
import com.example.historialmedico.ui.components.SelectorFechaDialog
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch

//formulario aparte, guardar lo hace la lista que sigue abierta al cerrarlo
@Composable
fun ExamenFormulario(
    paciente: Paciente,
    horasMedicas: List<HoraMedica>,
    onGuardar: (examen: Examen) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tipo by rememberSaveable { mutableStateOf("") }
    var realizado by rememberSaveable { mutableStateOf(false) }
    var fecha by rememberSaveable { mutableStateOf<String?>(null) }
    var resultado by rememberSaveable { mutableStateOf("") }
    //solo el id, una HoraMedica entera no se puede guardar al cambiar de pestaña
    var consultaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var documentoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var menuConsultaAbierto by remember { mutableStateOf(false) }
    //evita guardar dos veces si se toca Guardar dos veces seguidas
    var guardado by remember { mutableStateOf(false) }
    val consulta = horasMedicas.firstOrNull { it.id == consultaId }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val pagina = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
                ?.pages?.firstOrNull()
            if (pagina != null) {
                scope.launch {
                    val nuevo = Documentos.guardar(context, pagina.imageUri.toString())
                    if (nuevo == null) {
                        error = "No se pudo guardar el documento escaneado"
                    } else {
                        documentoUri?.let { Documentos.borrar(it) }
                        documentoUri = nuevo
                    }
                }
            }
        }
    }
    val scanner = remember { Documentos.crearEscaner() }

    fun guardar() {
        if (guardado) return
        error = ReglasTexto.validar(tipo, CampoTexto.TIPO_EXAMEN)
            ?: ReglasTexto.validar(resultado, CampoTexto.RESULTADO, obligatorio = false)
                    ?: ReglasExamen.validarFecha(realizado, fecha, Fechas.hoyIso())
                    ?: ReglasExamen.validarConsulta(
                fecha,
                consulta?.let { Fechas.millisAIso(it.fechaHora) }
            )
        if (error != null) return
        guardado = true
        onGuardar(
            Examen(
                pacienteId = paciente.id,
                tipo = ReglasTexto.normalizar(tipo),
                fecha = fecha,
                resultado = ReglasTexto.normalizar(resultado).ifBlank { null },
                estado = ReglasExamen.estado(realizado, fecha),
                documentoUri = documentoUri,
                horaMedicaId = consulta?.id
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = tipo,
            onValueChange = { if (it.length <= CampoTexto.TIPO_EXAMEN.maximo) tipo = it },
            label = { Text("Tipo de examen") },
            supportingText = { Text("${tipo.length}/${CampoTexto.TIPO_EXAMEN.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box {
            OutlinedButton(
                onClick = { menuConsultaAbierto = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    "Solicitado en: " +
                            (consulta?.let { ReglasExamen.textoConsulta(it) } ?: "sin consulta")
                )
            }
            DropdownMenu(
                expanded = menuConsultaAbierto,
                onDismissRequest = { menuConsultaAbierto = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin consulta") },
                    onClick = {
                        consultaId = null
                        menuConsultaAbierto = false
                    }
                )
                ReglasExamen.consultasPosibles(horasMedicas, System.currentTimeMillis())
                    .forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(ReglasExamen.textoConsulta(opcion)) },
                            onClick = {
                                consultaId = opcion.id
                                menuConsultaAbierto = false
                            }
                        )
                    }
            }
        }
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
        OutlinedButton(
            onClick = { mostrarSelectorFecha = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                fecha?.let { "Fecha: ${Fechas.isoAVisible(it)}" }
                    ?: if (realizado) "Elegir fecha en que se realizó" else "Elegir fecha (opcional)"
            )
        }
        if (fecha != null) {
            TextButton(onClick = { fecha = null }) { Text("Quitar fecha") }
        }
        if (realizado) {
            OutlinedTextField(
                value = resultado,
                onValueChange = { if (it.length <= CampoTexto.RESULTADO.maximo) resultado = it },
                label = { Text("Resultado (opcional)") },
                supportingText = { Text("${resultado.length}/${CampoTexto.RESULTADO.maximo}") },
                modifier = Modifier.fillMaxWidth()
            )
        }
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
            Text(
                if (documentoUri == null) "Escanear documento"
                else "Documento escaneado (volver a escanear)"
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
            Text("Guardar examen")
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
}