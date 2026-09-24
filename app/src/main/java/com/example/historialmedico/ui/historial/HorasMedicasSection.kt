package com.example.historialmedico.ui.historial

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Perfil
import kotlinx.coroutines.launch

@Composable
fun HorasMedicasSection(dao: HoraMedicaDao, perfil: Perfil){
    val horas by dao.getByPerfil(perfil.id).collectAsState(initial =
        emptyList())
    var especialidad by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var horaAEliminar by remember { mutableStateOf<HoraMedica?>(null) }
    var scope = rememberCoroutineScope()

    Column(modifier =
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            "Horas Medicas de ${perfil.nombre}", style =
                MaterialTheme.typography.titleMedium,
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${hora.especialidad}-${hora.fecha} (${hora.lugar})",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { horaAEliminar = hora }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar hora medica"
                        )
                    }
                }
            }
        }

        horaAEliminar?.let { hora->
            AlertDialog(
                onDismissRequest = {horaAEliminar=null},
                title = {Text("Eliminar hora medica")},
                text = {Text("Seguro que quiere eliminar esta hora medica?")},
                confirmButton={
                    TextButton(onClick = {
                        scope.launch { dao.delete(hora) }
                        horaAEliminar=null
                    }){
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {horaAEliminar=null}){
                        Text("Cancelar")
                    }
                }
            )
        }
        OutlinedTextField(
            value = especialidad,
            onValueChange = {especialidad=it},
            label = {Text("Especialidad")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fecha,
            onValueChange = {fecha=it},
            label = {Text("Fecha (ej: 18/09/2026)")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lugar,
            onValueChange = {lugar=it},
            label = {Text("Lugar")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick ={
            when {
                especialidad.isBlank() -> error = "La especialidad es obligatoria"
                fecha.isBlank() -> error = "La fecha es obligatoria"
                lugar.isBlank() -> error = "El lugar es obligatorio"
                else -> {
                    val especialidadAGuardar = especialidad.trim()
                    val fechaAGuardar = fecha.trim()
                    val lugarAGuardar = lugar.trim()
                    especialidad = ""
                    fecha = ""
                    lugar = ""
                    error = null
                    scope.launch {
                        dao.insert(
                            HoraMedica(
                                perfilId = perfil.id,
                                especialidad = especialidadAGuardar,
                                fecha = fechaAGuardar,
                                lugar = lugarAGuardar
                            )
                        )
                    }
                }
            }
        }){
            Text("Agregar Hora Medica")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error, style=MaterialTheme.typography.bodySmall)
        }
    }
}
