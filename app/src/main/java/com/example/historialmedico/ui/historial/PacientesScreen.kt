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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PacienteDao
import com.example.historialmedico.ui.perfil.CrearPerfilScreen

@Composable
fun PacientesScreen(
    perfilDao: PerfilDao,
    pacienteDao: PacienteDao,
    medicamentoDao: MedicamentoDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao,
    alarmaDao: AlarmaDao,
    modifier: Modifier=Modifier) {
    var cargando by remember { mutableStateOf(true) }
    var perfil by remember { mutableStateOf<Perfil?>(null) }
    var pacienteSeleccionado by remember { mutableStateOf<Paciente?>(null) }

    LaunchedEffect(perfilDao) {
        perfilDao.getPerfil().collect {
            perfil=it
            cargando=false
        }
    }

    val perfilActual=perfil
    val paciente=pacienteSeleccionado
    when{
        cargando->Box(modifier=modifier.fillMaxSize())
        perfilActual==null->CrearPerfilScreen(perfilDao=perfilDao, modifier=modifier)
        paciente!=null->PacienteDetalleScreen(
            paciente=paciente,
            medicamentoDao=medicamentoDao,
            horaMedicaDao=horaMedicaDao,
            examenDao=examenDao,
            alarmaDao=alarmaDao,
            modifier=modifier,
            onBack={pacienteSeleccionado=null}
        )
        else->ListaPacientesScreen(
           perfil=perfilActual,
            pacienteDao=pacienteDao,
            alarmaDao=alarmaDao,
            modifier=modifier,
            onPacienteClick={pacienteSeleccionado=it}
        )
    }
}

@Composable
private fun ListaPacientesScreen(
    perfil: Perfil,
    pacienteDao: PacienteDao,
    alarmaDao: AlarmaDao,
    modifier: Modifier= Modifier,
    onPacienteClick:(Paciente)-> Unit
){
    val pacientes by remember(perfil.id){pacienteDao.getByPerfil(perfil.id)
    }
        .collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    var relacion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    var pacienteAEliminar by remember { mutableStateOf<Paciente?>(null) }


    Column(modifier=modifier
        .fillMaxSize()
        .imePadding()
        .padding(16.dp)) {
        Text("Pacientes", style= MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(16.dp))

        LazyColumn(modifier= Modifier.weight(1f)) {
            if(pacientes.isEmpty()){
                item{
                    Text(
                        "No hay pacientes todavia, agrega uno abajo",
                        style=MaterialTheme.typography.bodyMedium,
                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier=Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(pacientes) { paciente ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable {
                            onPacienteClick(paciente)
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
                                paciente.nombre,
                                style=MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                paciente.relacion,
                                style= MaterialTheme.typography.bodySmall,
                                color= MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            pacienteAEliminar=paciente
                        }) {
                            Icon(
                                imageVector= Icons.Filled.Delete,
                                contentDescription = "Eliminar paciente"
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
                            val nombreAGuardar = nombre.trim()
                            val relacionAGuardar = relacion.trim()
                        if(nombreAGuardar.isBlank()||relacionAGuardar.isBlank()){
                        error = "El nombre y la relacion son obligatorios"
                    }else{
                            nombre = ""
                            relacion = ""
                        error=null
                            scope.launch {
                                pacienteDao.insert(
                                    Paciente(
                                        perfilId=perfil.id,
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
                error?.let {
                    Spacer(modifier= Modifier.height(8.dp))
                    Text(
                        it,
                        color= MaterialTheme.colorScheme.error,
                        style= MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        pacienteAEliminar?.let { paciente -> AlertDialog(
            onDismissRequest = {pacienteAEliminar=null},
            title = {Text("Eliminar paciente")},
            text = {Text("Seguro que quieres eliminar a ${paciente.nombre}? Se eliminaran tambien todos sus medicamentos, horas medicas y examenes.")},
            confirmButton = {
                TextButton(onClick = {
                    pacienteAEliminar=null
                    scope.launch { alarmaDao.getIdsByPaciente(paciente.id).forEach {
                        alarmaId->
                        AlarmScheduler.cancelar(context,alarmaId)
                    }
                    pacienteDao.delete(paciente)
                    }
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {pacienteAEliminar=null}) {
                    Text("Cancelar")
                }
            }
        ) }
    }
}