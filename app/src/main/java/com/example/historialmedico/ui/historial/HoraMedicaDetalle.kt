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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Alarma
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.util.Fechas

//android puede atrasar hasta una hora un recordatorio que no es exacto
private const val MARGEN_RECORDATORIO = 3_600_000L

//pantalla de una hora medica, eliminar esta aqui y no en la lista para no borrar por error
@Composable
fun HoraMedicaDetalle(
    horaMedica: HoraMedica,
    alarma: Alarma?,
    onEliminar: () -> Unit
) {
    val ahora = rememberAhora()
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            horaMedica.especialidad,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        BloqueDetalle("Fecha y hora") {
            Text(
                Fechas.fechaHoraVisible(horaMedica.fechaHora),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BloqueDetalle("Lugar") {
            Text(horaMedica.lugar, style = MaterialTheme.typography.bodyLarge)
        }
        BloqueDetalle("Recordatorio") {
            Text(textoRecordatorio(alarma, ahora), style = MaterialTheme.typography.bodyLarge)
        }
        BotonEliminar("Eliminar hora médica") { confirmarEliminar = true }
    }

    if (confirmarEliminar) {
        ConfirmarEliminar(
            titulo = "¿Eliminar la hora de ${horaMedica.especialidad}?",
            texto = if (alarma != null) {
                "También se borra su recordatorio. No se puede deshacer."
            } else {
                "No se puede deshacer."
            },
            onConfirmar = {
                confirmarEliminar = false
                onEliminar()
            },
            onCancelar = { confirmarEliminar = false }
        )
    }
}

//el recordatorio de una hora medica suena una sola vez y despues queda inactivo
private fun textoRecordatorio(alarma: Alarma?, ahora: Long): String {
    if (alarma == null) return "Sin recordatorio"
    val momento = alarma.fechaHoraMillis ?: return "Sin recordatorio"
    return when {
        !alarma.activo -> "Ya se envió"
        momento > ahora -> Fechas.fechaHoraVisible(momento)
        ahora - momento < MARGEN_RECORDATORIO -> "${Fechas.fechaHoraVisible(momento)}, por llegar"
        //paso la hora sin enviarse, por ejemplo con el telefono apagado o la app detenida
        else -> "No llegó a enviarse"
    }
}