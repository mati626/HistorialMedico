package com.example.historialmedico.ui.historial

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
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.ui.components.SelectorFechaHoraDialog
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HorasMedicasSection(dao: HoraMedicaDao, alarmaDao: AlarmaDao, paciente: Paciente) {
    val horas by remember(paciente.id) { dao.getByPaciente(paciente.id) }
        .collectAsState(initial = emptyList())
    val alarmas by remember(paciente.id) { alarmaDao.getDeHorasMedicas(paciente.id) }
        .collectAsState(initial = emptyList())
    val alarmasPorHoraMedica = alarmas.groupBy { it.horaMedicaId }
    var especialidad by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var fechaHora by remember { mutableStateOf<Long?>(null) }
    var anticipacion by remember { mutableStateOf<Anticipacion?>(null) }
    var mostrarSelectorFechaHora by remember { mutableStateOf(false) }
    var menuRecordatorioAbierto by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var horaAEliminar by remember { mutableStateOf<HoraMedica?>(null) }
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Horas Medicas de ${paciente.nombre}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        horas.forEach { hora ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${hora.especialidad} - ${Fechas.fechaHoraVisible(hora.fechaHora)}")
                        Text(
                            hora.lugar,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val alarma = alarmasPorHoraMedica[hora.id].orEmpty().firstOrNull()
                            val momentoRecordatorio=alarma?.fechaHoraMillis
                        if (alarma != null && momentoRecordatorio!=null) {
                            Text(
                                if(alarma.activo) "Recordatorio: ${Fechas.fechaHoraVisible(momentoRecordatorio)}"
                                else "Recordatorio enviado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { horaAEliminar = hora }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar hora medica",
                            tint= MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        horaAEliminar?.let { hora ->
            AlertDialog(
                onDismissRequest = { horaAEliminar = null },
                title = { Text("Eliminar hora medica") },
                text = { Text("Seguro que quiere eliminar esta hora medica?") },
                confirmButton = {
                    TextButton(onClick = {
                        alarmasPorHoraMedica[hora.id].orEmpty().forEach { alarma ->
                            AlarmScheduler.cancelar(appContext, alarma.id)
                        }
                        scope.launch { dao.delete(hora) }
                        horaAEliminar = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { horaAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        OutlinedTextField(
            value = especialidad,
            onValueChange = { especialidad = it },
            label = { Text("Especialidad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text("Lugar") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { mostrarSelectorFechaHora = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                fechaHora?.let { "Fecha y hora: ${Fechas.fechaHoraVisible(it)}" }
                    ?: "Elegir fecha y hora"
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box{
            OutlinedButton(
                onClick = {menuRecordatorioAbierto=true},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recordatorio: ${anticipacion?.etiqueta?: "sin recordatorio"}")
            }
            DropdownMenu(
                expanded = menuRecordatorioAbierto,
                onDismissRequest = {menuRecordatorioAbierto=false}
            ) {
                DropdownMenuItem(
                    text = {Text("Sin recordatorio")},
                    onClick={
                        anticipacion=null
                        menuRecordatorioAbierto = false
                    }
                )
                Anticipacion.entries.forEach{ opcion ->
                    DropdownMenuItem(
                        text={Text(opcion.etiqueta)},
                        onClick={
                            anticipacion=opcion
                            menuRecordatorioAbierto=false
                        }
                    )
                }
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
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val fechaHoraElegida = fechaHora
            val anticipacionElegida = anticipacion
            val ahora = System.currentTimeMillis()
            val recordatorioElegido=
                if (fechaHoraElegida!=null && anticipacionElegida != null)
                    fechaHoraElegida-anticipacionElegida.minutos * 60_000L
            else null
            when {
                especialidad.isBlank() -> error = "La especialidad es obligatoria"
                lugar.isBlank() -> error = "El lugar es obligatorio"
                fechaHoraElegida == null -> error = "La fecha y hora son obligatorias"
                recordatorioElegido != null && recordatorioElegido <= ahora ->
                    error = "Ese recordatorio ya paso: elige una anticipacion menor o quitalo"
                else -> {
                    val horaMedica = HoraMedica(
                        pacienteId = paciente.id,
                        especialidad = especialidad.trim(),
                        fechaHora = fechaHoraElegida,
                        lugar = lugar.trim()
                    )
                    especialidad = ""
                    lugar = ""
                    fechaHora = null
                    anticipacion = null
                    error = null
                    scope.launch {
                        withContext(NonCancellable) {
                            val alarma = dao.insertConRecordatorio(horaMedica, recordatorioElegido)
                            if (alarma != null) {
                                AlarmScheduler.programar(appContext, alarma)
                            }
                        }
                    }
                }
            }
        }) {
            Text("Agregar Hora Medica")
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
private enum class Anticipacion(val minutos: Int, val etiqueta: String){
    TREINTA_MINUTOS(30, "30 minutos antes"),
    UNA_HORA(60, "1 hora antes"),
    DOS_HORAS(120, "2 horas antes"),
    UN_DIA(1440, "1 dia antes")
}
