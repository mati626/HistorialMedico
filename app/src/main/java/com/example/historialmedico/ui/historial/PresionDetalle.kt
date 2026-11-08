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
import com.example.historialmedico.data.PresionArterial
import com.example.historialmedico.util.Fechas

//pantalla de una medicion, eliminar esta aqui y no en la lista para no borrar por error
@Composable
fun PresionDetalle(medicion: PresionArterial, onEliminar: () -> Unit) {
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "${medicion.sistolica}/${medicion.diastolica} mmHg",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        BloqueDetalle("Fecha y hora") {
            Text(
                Fechas.fechaHoraVisible(medicion.fechaHora),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BloqueDetalle("Pulso") {
            Text(
                medicion.pulso?.let { "$it lpm" } ?: "No se registró",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BotonEliminar("Eliminar medición") { confirmarEliminar = true }
    }

    if (confirmarEliminar) {
        ConfirmarEliminar(
            titulo = "¿Eliminar la medición ${medicion.sistolica}/${medicion.diastolica}?",
            texto = "No se puede deshacer.",
            onConfirmar = {
                confirmarEliminar = false
                onEliminar()
            },
            onCancelar = { confirmarEliminar = false }
        )
    }
}