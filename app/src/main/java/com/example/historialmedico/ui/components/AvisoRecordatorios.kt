package com.example.historialmedico.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.historialmedico.alarms.PermisosRecordatorios

//solo aparece si falta algo para que los recordatorios lleguen a tiempo
@Composable
fun AvisoRecordatorios(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var notificaciones by remember {
        mutableStateOf(PermisosRecordatorios.notificacionesActivas(context))
    }
    var bateria by remember {
        mutableStateOf(PermisosRecordatorios.bateriaSinRestriccion(context))
    }
    //al volver de los ajustes se revisa de nuevo
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificaciones = PermisosRecordatorios.notificacionesActivas(context)
        bateria = PermisosRecordatorios.bateriaSinRestriccion(context)
    }
    if (notificaciones && bateria) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Sus recordatorios podrían no llegar a tiempo", fontWeight = FontWeight.Bold)
            if (!notificaciones) {
                Text(
                    "Las notificaciones están desactivadas.",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = { PermisosRecordatorios.abrirAjustesNotificaciones(context) }) {
                    Text("Activar notificaciones")
                }
            }
            if (!bateria) {
                Text(
                    "El ahorro de batería puede atrasarlos o retenerlos.",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = { PermisosRecordatorios.pedirBateriaSinRestriccion(context) }) {
                    Text("Quitar la restricción")
                }
            }
        }
    }
}