package com.example.historialmedico.ui.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PresionArterial
import com.example.historialmedico.data.PresionArterialDao
import com.example.historialmedico.ui.components.SelectorFechaHoraDialog
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.launch

@Composable
fun PresionArterialSection(dao: PresionArterialDao,paciente: Paciente){
    val mediciones by remember(paciente.id){dao.getByPaciente(paciente.id)}
        .collectAsState(initial=emptyList())
    var sistolica by remember { mutableStateOf("") }
    var diastolica by remember { mutableStateOf("") }
    var pulso by remember { mutableStateOf("") }
    var fechaHora by remember { mutableStateOf<Long?>(null) }
    var mostrarSelectorFechaHora by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var medicionAEliminar by remember { mutableStateOf<PresionArterial?>(null) }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Presion arterial de ${paciente.nombre}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight= FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        mediciones.forEach { medicion->
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
                        Text("${medicion.sistolica}/${medicion.diastolica} mmHg")
                        Text(
                            buildString {
                                append(Fechas.fechaHoraVisible(medicion.fechaHora))
                                if (medicion.pulso!=null) append(" - Pulso: ${medicion.pulso} lpm")
                            },
                            style=MaterialTheme.typography.bodySmall,
                            color=MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {medicionAEliminar=medicion}) {
                        Icon(
                            imageVector=Icons.Filled.Delete,
                            contentDescription="Eliminar medicion"
                        )
                    }
                }
            }
        }
        medicionAEliminar?.let { medicion->
            AlertDialog(
                onDismissRequest = {medicionAEliminar=null},
                title = {Text("Eliminar medicion")},
                text = {Text("Seguro que quiere eliminar esta medicion?")},
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(medicion) }
                        medicionAEliminar=null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton={
                    TextButton(onClick = {medicionAEliminar=null}) {
                        Text("Cancelar")
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = sistolica,
                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) sistolica = it },
                label = { Text("Sistolica") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = diastolica,
                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) diastolica = it },
                label = { Text("Diastolica") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = pulso,
                onValueChange = {if (it.length <= 3 && it.all(Char::isDigit)) pulso=it},
                label = {Text("Pulso (opcional)")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {mostrarSelectorFechaHora=true},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                fechaHora?.let { "Fecha y hora: ${Fechas.fechaHoraVisible(it)}" }
                    ?: "Fecha y hora: ahora (tocar para cambiar)"
            )
        }
        if (mostrarSelectorFechaHora){
            SelectorFechaHoraDialog(
                onConfirmar = {
                    fechaHora=it
                    mostrarSelectorFechaHora=false
                },
                onCancelar = {mostrarSelectorFechaHora=false}
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val sistolicaNum = sistolica.toIntOrNull()
            val diastolicaNum = diastolica.toIntOrNull()
            val pulsoNum = pulso.toIntOrNull()
            val ahora = System.currentTimeMillis()
            val momento = fechaHora?: ahora
            when{
                sistolicaNum==null -> error ="La presion sistolica es obligatoria"
                diastolicaNum==null -> error ="La presion diastolica es obligatoria"
                sistolicaNum !in SISTOLICA_MIN..SISTOLICA_MAX-> error="La sistolica debe estar entre $SISTOLICA_MIN y $SISTOLICA_MAX"
                diastolicaNum !in DIASTOLICA_MIN..DIASTOLICA_MAX-> error="La diastolica debe estar entre $DIASTOLICA_MIN y $DIASTOLICA_MAX"
                sistolicaNum <= diastolicaNum -> error ="La sistolica debe ser mayor que la diastolica"
                pulso.isNotBlank() && (pulsoNum==null || pulsoNum !in PULSO_MIN..PULSO_MAX)-> error ="El pulso debe estar entre $PULSO_MIN y $PULSO_MAX"
                momento>ahora->error="La medicion no puede tener una fecha futura"
                else -> {
                    val medicion= PresionArterial(
                        pacienteId = paciente.id,
                        sistolica = sistolicaNum,
                        diastolica = diastolicaNum,
                        pulso = pulsoNum,
                        fechaHora = momento
                    )
                    sistolica=""
                    diastolica=""
                    pulso=""
                    fechaHora=null
                    error=null
                    scope.launch { dao.insert(medicion) }
                }
            }
        }) {
            Text("Agregar Medicion")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                it,
                color=MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private const val SISTOLICA_MIN=50
private const val SISTOLICA_MAX=300
private const val DIASTOLICA_MIN=30
private const val DIASTOLICA_MAX=200
private const val PULSO_MIN=30
private const val PULSO_MAX=250