package com.example.historialmedico.ui.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Perfil
import kotlinx.coroutines.launch

@Composable
fun MedicamentosSection (dao: MedicamentoDao,perfil: Perfil) {
    val medicamentos by dao.getByPerfil(perfil.id).collectAsState(initial = emptyList())
    var medicamentoAEliminar by remember{mutableStateOf<Medicamento?>(null)}
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(
            "Medicamentos de ${perfil.nombre}", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        medicamentos.forEach { medicamento ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (medicamento.dosis.isBlank()) medicamento.nombre
                        else "${medicamento.nombre}- ${medicamento.dosis}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { medicamentoAEliminar = medicamento }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar medicamento"
                        )
                    }
                }
            }
        }
        medicamentoAEliminar?.let { medicamento ->
            AlertDialog(
                onDismissRequest = { medicamentoAEliminar = null },
                title = { Text("Eliminar Medicamento") },
                text = { Text("Seguro que quieres eliminar ${medicamento.nombre}?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(medicamento) }
                        medicamentoAEliminar = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        medicamentoAEliminar = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = dosis,
                onValueChange = { dosis = it },
                label = { Text("Dosis") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                nombre.isBlank()->error="El nombre del medicamento es obligatorio"
                dosis.isBlank()->error="La dosis es obligatoria"
                !dosis.any{it.isDigit()}->error="La dosis debe incluir un numero (ej:500mg)"
                medicamentos.size>=MAX_MEDICAMENTOS_POR_PERFIL->error="Este perfil ya tiene el maximo de $MAX_MEDICAMENTOS_POR_PERFIL medicamentos"
                medicamentos.any{it.nombre.trim().equals(nombre.trim(),ignoreCase = true)}->error="Ya existe un medicamento con ese nombre para este perfil"
                else->{
                    val nombreAGuardar=nombre.trim()
                    val dosisAGuardar=dosis.trim()
                    nombre=""
                    dosis=""
                    error=null
                    scope.launch {
                        dao.insert(
                            Medicamento(
                                perfilId = perfil.id,
                                nombre = nombreAGuardar,
                                dosis=dosisAGuardar
                            )
                        )
                    }
                }
            }
        }) {
            Text("Agregar Medicamento")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
private const val MAX_MEDICAMENTOS_POR_PERFIL=10