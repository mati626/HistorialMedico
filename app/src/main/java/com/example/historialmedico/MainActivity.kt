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
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import kotlinx.coroutines.launch

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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PerfilesScreen(dao: PerfilDao, modifier: Modifier=Modifier){
    val perfiles by dao.getAll().collectAsState(initial = emptyList())
    var nombre by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier=modifier.fillMaxSize().padding(16.dp)) {
        Text("Perfiles", style= MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(16.dp))

        LazyColumn(modifier= Modifier.weight(1f)) {
            items(perfiles) { perfil ->
                Row(
                    modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${perfil.nombre} (${perfil.relacion})",
                        modifier= Modifier.weight(1f)
                    )
                    IconButton(onClick =  {
                        scope.launch {
                            dao.delete(perfil)
                        }
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
    }
}

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