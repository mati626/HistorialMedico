package com.example.historialmedico.ui.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Perfil
import com.example.historialmedico.data.PerfilDao
import kotlinx.coroutines.launch

@Composable
fun PerfilesScreen(dao: PerfilDao, medicamentoDao: MedicamentoDao,horaMedicaDao: HoraMedicaDao, examenDao: ExamenDao, modifier: Modifier=Modifier) {
    var perfilSeleccionado by remember { mutableStateOf<Perfil?>(null) }
    val perfil = perfilSeleccionado
    if (perfil != null) {
        PerfilDetalleScreen(
            perfil = perfil,
            medicamentoDao = medicamentoDao,
            horaMedicaDao = horaMedicaDao,
            examenDao = examenDao,
            modifier = modifier,
            onBack = { perfilSeleccionado = null }
        )
    } else {
        ListaPerfilesScreen(
            dao = dao,
            modifier = modifier,
            onPerfilClick = { perfilSeleccionado = it }
        )
    }
}

@Composable
private fun ListaPerfilesScreen(dao: PerfilDao,modifier: Modifier= Modifier, onPerfilClick:(Perfil)-> Unit){
    val perfiles by dao.getAll().collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    var relacion by remember { mutableStateOf("") }
    val scope=rememberCoroutineScope()
    var perfilAEliminar by remember { mutableStateOf<Perfil?>(null) }


    Column(modifier=modifier
        .fillMaxSize()
        .imePadding()
        .padding(16.dp)) {
        Text("Perfiles", style= MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(16.dp))

        LazyColumn(modifier= Modifier.weight(1f)) {
            if(perfiles.isEmpty()){
                item{
                    Text(
                        "No hay perfiles todavia,agrega uno abajo",
                        style=MaterialTheme.typography.bodyMedium,
                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier=Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(perfiles) { perfil ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable {
                            onPerfilClick(perfil)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier=Modifier.weight(1f)) {
                            Text(
                                perfil.nombre,
                                style=MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                perfil.relacion,
                                style= MaterialTheme.typography.bodySmall,
                                color= MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            perfilAEliminar=perfil
                        }) {
                            Icon(
                                imageVector= Icons.Filled.Delete,
                                contentDescription = "Eliminar perfil"
                            )
                        }
                    }
                }
            }
        }
        Card(
            modifier=Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp,MaterialTheme.colorScheme.outline)) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = relacion,
                    onValueChange = { relacion = it },
                    label = { Text("Relacion (ej:Yo, Hijo/a, Padre)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (nombre.isNotBlank() && relacion.isNotBlank()) {
                            val nombreAGuardar = nombre
                            val relacionAGuardar = relacion
                            nombre = ""
                            relacion = ""
                            scope.launch {
                                dao.insert(
                                    Perfil(
                                        nombre = nombreAGuardar,
                                        relacion = relacionAGuardar
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar")
                }
            }
        }
        perfilAEliminar?.let { perfil -> AlertDialog(
            onDismissRequest = {perfilAEliminar=null},
            title = {Text("Eliminar perfil")},
            text = {Text("Seguro que quieres eliminar a ${perfil.nombre}? Se eliminaran tambien todos sus medicamentos.")},
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { dao.delete(perfil) }
                    perfilAEliminar=null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {perfilAEliminar=null}) {
                    Text("Cancelar")
                }
            }
        ) }
    }
}