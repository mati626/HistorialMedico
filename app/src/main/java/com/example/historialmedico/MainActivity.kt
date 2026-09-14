package com.example.historialmedico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.AppDataBase
import com.example.historialmedico.data.Perfil
import com.example.historialmedico.data.PerfilDao
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDataBase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        database= AppDataBase.getInstance(this)
        setContent {
            HistorialMedicoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PerfilesScreen(
                        dao= database.perfilDao(),
                        medicamentoDao= database.medicamentoDao(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PerfilesScreen(dao: PerfilDao, medicamentoDao: MedicamentoDao, modifier: Modifier=Modifier){
    val perfiles by dao.getAll().collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var perfilSeleccionado by remember { mutableStateOf<Perfil?>(null) }
    var perfilAEliminar by remember { mutableStateOf<Perfil?>(null) }



    Column(modifier=modifier
        .fillMaxSize()
        .imePadding()
        .padding(16.dp)) {
        Text("Perfiles", style= MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(16.dp))

        LazyColumn(modifier= Modifier.weight(1f)) {
            items(perfiles) { perfil ->
                Row(
                    modifier=Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            perfilSeleccionado = perfil
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${perfil.nombre} (${perfil.relacion})",
                        modifier= Modifier.weight(1f)
                    )
                    IconButton(onClick =  {
                        perfilAEliminar=perfil
                    }){
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar perfil")
                }
                }
            }
        }
            Row(verticalAlignment= Alignment.CenterVertically){
                OutlinedTextField(
                    value=nombre,
                    onValueChange = {nombre= it},
                    label = {Text("Nombre")},
                    modifier= Modifier.weight(1f)
                )
            Spacer(modifier= Modifier.width(8.dp))
            Button(onClick={
                if(nombre.isNotBlank()){
                    scope.launch {
                        dao.insert(Perfil(nombre=nombre,relacion="Yo"))
                    }
                    nombre = ""
                }
            }) {
                Text("Agregar")
            }
        }
        perfilSeleccionado?.let { perfil ->
            Spacer(modifier=Modifier.height(24.dp))
            MedicamentosSection(dao=medicamentoDao,perfil=perfil)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (medicamento.dosis.isBlank()) medicamento.nombre
                    else "${medicamento.nombre}- ${medicamento.dosis}",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { medicamentoAEliminar=medicamento }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar medicamento"
                    )
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


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HistorialMedicoTheme {
        Greeting("Android")
    }
}