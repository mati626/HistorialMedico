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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.EstadoExamen
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//lista de examenes, el detalle y el formulario se abren en su propia pantalla
@Composable
fun ExamenesSection(
    dao: ExamenDao,
    horaMedicaDao: HoraMedicaDao,
    paciente: Paciente,
    detalleId: Long?,
    agregando: Boolean,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit,
    onVolver: () -> Unit
) {
    //null hasta que Room responde, asi no aparece "no hay examenes" por un instante
    val examenes = remember(paciente.id) { dao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val horasMedicas = remember(paciente.id) { horaMedicaDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    //este scope sigue vivo al cerrar el detalle o el formulario, asi guardar o borrar no se corta
    val scope = rememberCoroutineScope()

    when {
        agregando -> ExamenFormulario(
            paciente = paciente,
            horasMedicas = horasMedicas.orEmpty(),
            onGuardar = { examen ->
                scope.launch { withContext(NonCancellable) { dao.insert(examen) } }
                onVolver()
            }
        )

        detalleId != null -> {
            val examen = examenes?.firstOrNull { it.id == detalleId }
            when {
                examenes == null || horasMedicas == null -> Unit //todavia cargando
                //ya no existe, por ejemplo si se borro y la app se reabrio en el detalle
                examen == null -> LaunchedEffect(detalleId) { onVolver() }
                else -> ExamenDetalle(
                    examen = examen,
                    consulta = horasMedicas.firstOrNull { it.id == examen.horaMedicaId },
                    onMarcarRealizado = { fecha, resultado ->
                        scope.launch {
                            dao.update(
                                examen.copy(
                                    estado = EstadoExamen.REALIZADO,
                                    fecha = fecha,
                                    resultado = resultado
                                )
                            )
                        }
                    },
                    onEliminar = {
                        scope.launch {
                            withContext(NonCancellable) {
                                dao.delete(examen)
                                examen.documentoUri?.let { Documentos.borrar(it) }
                            }
                        }
                        onVolver()
                    }
                )
            }
        }

        else -> ListaExamenes(examenes, onVerDetalle, onAgregar)
    }
}

@Composable
private fun ListaExamenes(
    examenes: List<Examen>?,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit
) {
    //los pendientes arriba, los realizados abajo del mas reciente al mas antiguo
    val (pendientes, realizados) = examenes.orEmpty()
        .partition { it.estado != EstadoExamen.REALIZADO }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (examenes != null && examenes.isEmpty()) {
                Text(
                    "Todavía no hay exámenes. Agregue el primero con el botón de abajo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (pendientes.isNotEmpty()) {
                Text(
                    "Pendientes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                pendientes.forEach { examen ->
                    TarjetaExamen(examen, onClick = { onVerDetalle(examen.id) })
                }
            }
            if (realizados.isNotEmpty()) {
                Text(
                    "Realizados",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                realizados.asReversed().forEach { examen ->
                    TarjetaExamen(examen, onClick = { onVerDetalle(examen.id) })
                }
            }
        }
        BotonAgregar(
            texto = "Agregar examen",
            habilitado = examenes != null,
            onClick = onAgregar
        )
    }
}

@Composable
private fun TarjetaExamen(examen: Examen, onClick: () -> Unit) {
    TarjetaLista(onClick) {
        Text(
            examen.tipo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        //"Agendado: 13/11/2026", o solo el estado si no tiene fecha
        Text(
            examen.fecha?.let { "${examen.estado.etiqueta}: ${Fechas.isoAVisible(it)}" }
                ?: examen.estado.etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (examen.documentoUri != null) {
            Text(
                "Con documento escaneado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}