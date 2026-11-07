package com.example.historialmedico.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.Alarma
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.ReglasPastillas
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal const val MAX_MEDICAMENTOS_POR_PACIENTE = 10

//lista de medicamentos, el detalle y el formulario se abren en su propia pantalla
@Composable
fun MedicamentosSection(
    dao: MedicamentoDao,
    alarmaDao: AlarmaDao,
    paciente: Paciente,
    detalleId: Long?,
    agregando: Boolean,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit,
    onVolver: () -> Unit
) {
    //null hasta que Room responde, asi no aparece "no hay medicamentos" por un instante
    val medicamentos = remember(paciente.id) { dao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val alarmas = remember(paciente.id) { alarmaDao.getDeMedicamentos(paciente.id) }
        .collectAsState(initial = null).value
    //este scope sigue vivo al cerrar el detalle o el formulario, asi guardar o borrar no se corta
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext

    when {
        agregando -> MedicamentoFormulario(
            paciente = paciente,
            existentes = medicamentos.orEmpty(),
            onGuardar = { medicamento, horas ->
                scope.launch {
                    withContext(NonCancellable) {
                        dao.insertConAlarmas(medicamento, horas).forEach {
                            AlarmScheduler.programar(appContext, it)
                        }
                    }
                }
                onVolver()
            }
        )

        detalleId != null -> {
            val medicamento = medicamentos?.firstOrNull { it.id == detalleId }
            when {
                medicamentos == null || alarmas == null -> Unit //todavia cargando
                //ya no existe, por ejemplo si se borro y la app se reabrio en el detalle
                medicamento == null -> LaunchedEffect(detalleId) { onVolver() }
                else -> {
                    val alarmasDelMedicamento = alarmas.filter { it.medicamentoId == medicamento.id }
                    MedicamentoDetalle(
                        medicamento = medicamento,
                        horas = horasDe(alarmasDelMedicamento),
                        onReponer = { pastillas ->
                            scope.launch { dao.reponer(medicamento.id, pastillas) }
                        },
                        onEliminar = {
                            alarmasDelMedicamento.forEach {
                                AlarmScheduler.cancelar(appContext, it.id)
                            }
                            scope.launch {
                                withContext(NonCancellable) {
                                    dao.delete(medicamento)
                                    medicamento.documentoUri?.let { Documentos.borrar(it) }
                                }
                            }
                            onVolver()
                        }
                    )
                }
            }
        }

        else -> ListaMedicamentos(
            //espera tambien a las alarmas para no mostrar "Sin recordatorio" un instante
            medicamentos = if (alarmas == null) null else medicamentos,
            alarmas = alarmas.orEmpty(),
            onVerDetalle = onVerDetalle,
            onAgregar = onAgregar
        )
    }
}

@Composable
private fun ListaMedicamentos(
    medicamentos: List<Medicamento>?,
    alarmas: List<Alarma>,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit
) {
    val lleno = (medicamentos?.size ?: 0) >= MAX_MEDICAMENTOS_POR_PACIENTE
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (medicamentos != null && medicamentos.isEmpty()) {
                Text(
                    "Todavía no hay medicamentos. Agregue el primero con el botón de abajo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            medicamentos?.forEach { medicamento ->
                TarjetaMedicamento(
                    medicamento = medicamento,
                    horas = horasDe(alarmas.filter { it.medicamentoId == medicamento.id }),
                    onClick = { onVerDetalle(medicamento.id) }
                )
            }
        }
        if (lleno) {
            Text(
                "Llegó al máximo de $MAX_MEDICAMENTOS_POR_PACIENTE medicamentos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        //fijo abajo para que siempre se vea aunque la lista sea larga
        BotonAgregar(
            texto = "Agregar medicamento",
            habilitado = medicamentos != null && !lleno,
            onClick = onAgregar
        )
    }
}

@Composable
private fun TarjetaMedicamento(medicamento: Medicamento, horas: List<Int>, onClick: () -> Unit) {
    TarjetaLista(onClick) {
        Text(
            "${medicamento.nombre} ${medicamento.dosis}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            textoHorarios(horas),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        medicamento.cantidad?.let { cantidad ->
            TextoPastillas(cantidad, medicamento.unidadesPorToma, horas.size)
        }
    }
}

//pastillas que quedan, el aviso va en texto y no solo en rojo, para quien no distingue el color
@Composable
internal fun TextoPastillas(cantidad: Int, unidadesPorToma: Int, tomasPorDia: Int) {
    val dias = ReglasPastillas.diasRestantes(cantidad, unidadesPorToma, tomasPorDia)
    val pocas = ReglasPastillas.quedanPocas(dias)
    Text(
        ReglasPastillas.textoRestantes(cantidad, dias),
        style = MaterialTheme.typography.bodyMedium,
        color = if (pocas) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
    if (pocas) {
        Text(
            "Reponga pronto",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

//horas de los recordatorios, ordenadas
internal fun horasDe(alarmas: List<Alarma>): List<Int> = alarmas.mapNotNull { it.hora }.sorted()

//[480, 1200] -> "A las 08:00 y 20:00"
internal fun textoHorarios(horas: List<Int>): String {
    val textos = horas.map { Fechas.minutosAHora(it) }
    return when (textos.size) {
        0 -> "Sin recordatorio"
        1 -> "A las ${textos[0]}"
        else -> "A las " + textos.dropLast(1).joinToString(", ") + " y " + textos.last()
    }
}