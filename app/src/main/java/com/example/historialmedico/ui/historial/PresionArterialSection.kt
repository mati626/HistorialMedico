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
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PresionArterial
import com.example.historialmedico.data.PresionArterialDao
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//lista de mediciones, el detalle y el formulario se abren en su propia pantalla
@Composable
fun PresionArterialSection(
    dao: PresionArterialDao,
    paciente: Paciente,
    detalleId: Long?,
    agregando: Boolean,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit,
    onVolver: () -> Unit
) {
    //null hasta que Room responde, asi no aparece "no hay mediciones" por un instante
    val mediciones = remember(paciente.id) { dao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    //este scope sigue vivo al cerrar el detalle o el formulario, asi guardar o borrar no se corta
    val scope = rememberCoroutineScope()

    when {
        agregando -> PresionFormulario(
            paciente = paciente,
            onGuardar = { medicion ->
                scope.launch { withContext(NonCancellable) { dao.insert(medicion) } }
                onVolver()
            }
        )

        detalleId != null -> {
            val medicion = mediciones?.firstOrNull { it.id == detalleId }
            when {
                mediciones == null -> Unit //todavia cargando
                //ya no existe, por ejemplo si se borro y la app se reabrio en el detalle
                medicion == null -> LaunchedEffect(detalleId) { onVolver() }
                else -> PresionDetalle(
                    medicion = medicion,
                    onEliminar = {
                        scope.launch { withContext(NonCancellable) { dao.delete(medicion) } }
                        onVolver()
                    }
                )
            }
        }

        else -> ListaPresiones(mediciones, onVerDetalle, onAgregar)
    }
}

@Composable
private fun ListaPresiones(
    mediciones: List<PresionArterial>?,
    onVerDetalle: (Long) -> Unit,
    onAgregar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (mediciones != null && mediciones.isEmpty()) {
                Text(
                    "Todavía no hay mediciones. Agregue la primera con el botón de abajo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            //el DAO las entrega de la mas nueva a la mas antigua
            mediciones?.forEach { medicion ->
                TarjetaPresion(medicion, onClick = { onVerDetalle(medicion.id) })
            }
        }
        BotonAgregar(
            texto = "Agregar medición",
            habilitado = mediciones != null,
            onClick = onAgregar
        )
    }
}

@Composable
private fun TarjetaPresion(medicion: PresionArterial, onClick: () -> Unit) {
    TarjetaLista(onClick) {
        Text(
            "${medicion.sistolica}/${medicion.diastolica} mmHg",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            Fechas.fechaHoraVisible(medicion.fechaHora),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        medicion.pulso?.let {
            Text(
                "Pulso: $it lpm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}