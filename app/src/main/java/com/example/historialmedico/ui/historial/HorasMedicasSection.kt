package com.example.historialmedico.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.historialmedico.data.ExamenDao
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//lista de horas medicas, el detalle y el formulario se abren en su propia pantalla
@Composable
fun HorasMedicasSection(
    dao: HoraMedicaDao,
    alarmaDao: AlarmaDao,
    examenDao: ExamenDao,
    paciente: Paciente,
    detalleId: Long?,
    agregando: Boolean,
    editando: Boolean,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit,
    onEditar: () -> Unit,
    onVolver: () -> Unit
) {
    //null hasta que Room responde, asi no aparece "no hay horas" por un instante
    val horas = remember(paciente.id) { dao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val alarmas = remember(paciente.id) { alarmaDao.getDeHorasMedicas(paciente.id) }
        .collectAsState(initial = null).value
    //para no mover una consulta a despues de un examen pedido en ella
    val examenes = remember(paciente.id) { examenDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    //este scope sigue vivo al cerrar el detalle o el formulario, asi guardar o borrar no se corta
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext

    when {
        agregando -> HoraMedicaFormulario(
            paciente = paciente,
            inicial = null,
            onGuardar = { horaMedica, recordatorio ->
                scope.launch {
                    withContext(NonCancellable) {
                        dao.insertConRecordatorio(horaMedica, recordatorio)?.let {
                            AlarmScheduler.programar(appContext, it)
                        }
                    }
                }
                onVolver()
            }
        )

        detalleId != null -> {
            val horaMedica = horas?.firstOrNull { it.id == detalleId }
            val alarmasDeLaHora = alarmas.orEmpty().filter { it.horaMedicaId == detalleId }
            val alarma = alarmasDeLaHora.firstOrNull()
            when {
                horas == null || alarmas == null || examenes == null -> Unit //todavia cargando
                //ya no existe, por ejemplo si se borro y la app se reabrio en el detalle
                //editando en la clave: si estaba editando vuelve dos veces, hasta la lista
                horaMedica == null -> LaunchedEffect(detalleId, editando) { onVolver() }
                editando -> HoraMedicaFormulario(
                    paciente = paciente,
                    inicial = horaMedica,
                    recordatorioInicial = alarma?.fechaHoraMillis,
                    examenesPedidos = examenes.filter { it.horaMedicaId == horaMedica.id },
                    onGuardar = { cambiada, recordatorio ->
                        if (recordatorio == alarma?.fechaHoraMillis) {
                            //mismo recordatorio, la alarma queda como estaba aunque ya se haya enviado
                            scope.launch { withContext(NonCancellable) { dao.update(cambiada) } }
                        } else {
                            alarma?.let { AlarmScheduler.cancelar(appContext, it.id) }
                            scope.launch {
                                withContext(NonCancellable) {
                                    dao.updateConRecordatorio(cambiada, alarma, recordatorio)?.let {
                                        AlarmScheduler.programar(appContext, it)
                                    }
                                }
                            }
                        }
                        onVolver()
                    }
                )

                else -> HoraMedicaDetalle(
                    horaMedica = horaMedica,
                    alarma = alarma,
                    onEditar = onEditar,
                    onEliminar = {
                        alarmasDeLaHora.forEach { AlarmScheduler.cancelar(appContext, it.id) }
                        scope.launch {
                            withContext(NonCancellable) { dao.delete(horaMedica) }
                        }
                        onVolver()
                    }
                )
            }
        }

        else -> ListaHorasMedicas(horas, onVerDetalle, onAgregar)
    }
}

@Composable
private fun ListaHorasMedicas(
    horas: List<HoraMedica>?,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit
) {
    val ahora = rememberAhora()
    //las que vienen van arriba, las que ya pasaron abajo de la mas reciente a la mas antigua
    val (proximas, anteriores) = horas.orEmpty().partition { it.fechaHora > ahora }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (horas != null && horas.isEmpty()) {
                Text(
                    "Todavía no hay horas médicas. Agregue la primera con el botón de abajo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (proximas.isNotEmpty()) {
                Text(
                    "Próximas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                proximas.forEach { hora ->
                    TarjetaHoraMedica(hora, onClick = { onVerDetalle(hora.id) })
                }
            }
            if (anteriores.isNotEmpty()) {
                Text(
                    "Anteriores",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                anteriores.asReversed().forEach { hora ->
                    TarjetaHoraMedica(hora, onClick = { onVerDetalle(hora.id) })
                }
            }
        }
        BotonAgregar(
            texto = "Agregar hora médica",
            habilitado = horas != null,
            onClick = onAgregar
        )
    }
}

@Composable
private fun TarjetaHoraMedica(horaMedica: HoraMedica, onClick: () -> Unit) {
    TarjetaLista(onClick) {
        Text(
            horaMedica.especialidad,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            Fechas.fechaHoraVisible(horaMedica.fechaHora),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            horaMedica.lugar,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
