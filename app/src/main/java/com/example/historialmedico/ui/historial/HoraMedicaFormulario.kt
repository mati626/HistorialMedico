package com.example.historialmedico.ui.historial

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.ui.components.SelectorFechaHoraDialog
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto

//cuanto antes de la consulta llega el recordatorio
internal enum class Anticipacion(val minutos: Int, val etiqueta: String) {
    TREINTA_MINUTOS(30, "30 minutos antes"),
    UNA_HORA(60, "1 hora antes"),
    DOS_HORAS(120, "2 horas antes"),
    UN_DIA(1440, "1 día antes")
}

//formulario aparte, guardar lo hace la lista que sigue abierta al cerrarlo
@Composable
fun HoraMedicaFormulario(
    paciente: Paciente,
    onGuardar: (horaMedica: HoraMedica, recordatorioMillis: Long?) -> Unit
) {
    var especialidad by rememberSaveable { mutableStateOf("") }
    var lugar by rememberSaveable { mutableStateOf("") }
    var fechaHora by rememberSaveable { mutableStateOf<Long?>(null) }
    var anticipacion by rememberSaveable { mutableStateOf<Anticipacion?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarSelectorFechaHora by remember { mutableStateOf(false) }
    var menuRecordatorioAbierto by remember { mutableStateOf(false) }
    //evita guardar dos veces si se toca Guardar dos veces seguidas
    var guardado by remember { mutableStateOf(false) }

    fun guardar() {
        if (guardado) return
        val fechaHoraElegida = fechaHora
        val anticipacionElegida = anticipacion
        val recordatorio = if (fechaHoraElegida != null && anticipacionElegida != null) {
            fechaHoraElegida - anticipacionElegida.minutos * 60_000L
        } else {
            null
        }
        error = ReglasTexto.validar(especialidad, CampoTexto.ESPECIALIDAD)
            ?: ReglasTexto.validar(lugar, CampoTexto.LUGAR)
                    ?: when {
                fechaHoraElegida == null -> "Elija la fecha y la hora"
                recordatorio != null && recordatorio <= System.currentTimeMillis() ->
                    "Ese recordatorio ya pasó: elija una anticipación menor o quítelo"
                else -> null
            }
        if (error != null || fechaHoraElegida == null) return
        guardado = true
        onGuardar(
            HoraMedica(
                pacienteId = paciente.id,
                especialidad = ReglasTexto.normalizar(especialidad),
                fechaHora = fechaHoraElegida,
                lugar = ReglasTexto.normalizar(lugar)
            ),
            recordatorio
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
            value = especialidad,
            onValueChange = { if (it.length <= CampoTexto.ESPECIALIDAD.maximo) especialidad = it },
            label = { Text("Especialidad") },
            supportingText = { Text("${especialidad.length}/${CampoTexto.ESPECIALIDAD.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lugar,
            onValueChange = { if (it.length <= CampoTexto.LUGAR.maximo) lugar = it },
            label = { Text("Lugar") },
            supportingText = { Text("${lugar.length}/${CampoTexto.LUGAR.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = { mostrarSelectorFechaHora = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                fechaHora?.let { "Fecha y hora: ${Fechas.fechaHoraVisible(it)}" }
                    ?: "Elegir fecha y hora"
            )
        }
        Box {
            OutlinedButton(
                onClick = { menuRecordatorioAbierto = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Recordatorio: ${anticipacion?.etiqueta ?: "sin recordatorio"}")
            }
            DropdownMenu(
                expanded = menuRecordatorioAbierto,
                onDismissRequest = { menuRecordatorioAbierto = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin recordatorio") },
                    onClick = {
                        anticipacion = null
                        menuRecordatorioAbierto = false
                    }
                )
                Anticipacion.entries.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion.etiqueta) },
                        onClick = {
                            anticipacion = opcion
                            menuRecordatorioAbierto = false
                        }
                    )
                }
            }
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
            Text("Guardar hora médica")
        }
    }

    if (mostrarSelectorFechaHora) {
        SelectorFechaHoraDialog(
            onConfirmar = {
                fechaHora = it
                mostrarSelectorFechaHora = false
            },
            onCancelar = { mostrarSelectorFechaHora = false }
        )
    }
}