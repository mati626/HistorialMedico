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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.historialmedico.export.PdfExporter
import kotlinx.coroutines.flow.first

@Composable
fun PerfilesScreen(dao: PerfilDao, medicamentoDao: MedicamentoDao,horaMedicaDao: HoraMedicaDao, examenDao: ExamenDao, modifier: Modifier=Modifier){
    val perfiles by dao.getAll().collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    var relacion by remember {mutableStateOf("")}
    val scope = rememberCoroutineScope()
    val context=LocalContext.current
    var perfilSeleccionado by remember { mutableStateOf<Perfil?>(null) }
    var perfilAEliminar by remember { mutableStateOf<Perfil?>(null) }
    var tabSeleccionado by remember { mutableStateOf(0) }



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
                            perfilSeleccionado = perfil
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (perfil == perfilSeleccionado) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
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
                                dao.insert(Perfil(nombre = nombreAGuardar, relacion = relacionAGuardar))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar")
                }
            }
        }

        perfilSeleccionado?.let { perfil ->
            Spacer(modifier= Modifier.height(24.dp))
            TabRow(selectedTabIndex = tabSeleccionado) {
                Tab(
                    selected = tabSeleccionado==0,
                    onClick = {tabSeleccionado=0},
                    text = {Text("Medicamentos")}
                )
                Tab(
                    selected = tabSeleccionado==1,
                    onClick = {tabSeleccionado=1},
                    text = {Text("Horas Medicas")}
                )
                Tab(
                    selected = tabSeleccionado==2,
                    onClick = {tabSeleccionado=2},
                    text = {Text("Examenes")}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            when(tabSeleccionado){
                0->MedicamentosSection(dao=medicamentoDao, perfil = perfil)
                1->HorasMedicasSection(dao=horaMedicaDao,perfil=perfil)
                2->ExamenesSection(dao = examenDao, perfil = perfil)
            }
            Spacer(modifier= Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        val medicamentos=medicamentoDao.getByPerfil(perfil.id).first()
                        val horasMedicas=horaMedicaDao.getByPerfil(perfil.id).first()
                        val examenes=examenDao.getByPerfil(perfil.id).first()
                        val uri= PdfExporter.generarPdf(context,perfil,medicamentos,horasMedicas,examenes)
                        val intent= Intent(Intent.ACTION_SEND).apply {
                            type="application/pdf"
                            putExtra(Intent.EXTRA_STREAM,uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent,"Exportar historial"))
                    }
                },
                modifier= Modifier.fillMaxWidth()
            ) {
                Text("Exportar PDF")
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