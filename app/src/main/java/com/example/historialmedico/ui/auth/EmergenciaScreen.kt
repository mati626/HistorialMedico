package com.example.historialmedico.ui.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.PacienteDao
import com.example.historialmedico.data.PacienteEmergencia
import com.example.historialmedico.data.ReglasEmergencia
import com.example.historialmedico.data.ReglasPaciente
import com.example.historialmedico.ui.historial.BarraVolver
import com.example.historialmedico.ui.historial.NombreYDosis
import com.example.historialmedico.ui.historial.rememberAhora
import com.example.historialmedico.util.Fechas

//se abre desde el bloqueo sin PIN, solo lee los pacientes que el dueño activo en su ficha
@Composable
fun EmergenciaScreen(
    pacienteDao: PacienteDao,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    //null mientras Room responde, asi no aparece el aviso de lista vacia por un momento
    val pacientes by remember(pacienteDao) { pacienteDao.getParaEmergencia() }.collectAsState(initial = null)
    //se renueva al volver a la app, asi la edad queda al dia
    val hoy = Fechas.millisAIso(rememberAhora())

    fun llamar(telefono: String) {
        //ACTION_DIAL abre el marcador con el numero escrito y la persona aprieta llamar, por eso no pide permiso
        val marcar = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ReglasEmergencia.paraMarcar(telefono)))
        try {
            context.startActivity(marcar)
        } catch (e: ActivityNotFoundException) {
            //una tablet sin telefono no tiene marcador
            Toast.makeText(context, "Este equipo no puede hacer llamadas", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BarraVolver(titulo = null, onVolver = onVolver)
        //en rojo para que se reconozca de inmediato que es informacion de emergencia
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.large)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Información de emergencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "Datos que el dueño del teléfono decidió mostrar sin PIN.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        val lista = pacientes
        when {
            //cargando, no se muestra nada
            lista == null -> {}
            lista.isEmpty() -> Text(
                "No hay información de emergencia para mostrar.",
                style = MaterialTheme.typography.bodyLarge
            )

            else -> lista.forEach { datos ->
                TarjetaPacienteEmergencia(datos = datos, hoy = hoy, onLlamar = { llamar(it) })
            }
        }
    }
}

//un paciente con lo que necesita saber quien lo atiende, en el orden en que lo necesita
@Composable
private fun TarjetaPacienteEmergencia(datos: PacienteEmergencia, hoy: String, onLlamar: (String) -> Unit) {
    val paciente = datos.paciente
    val contacto = paciente.contactoNombre
    val telefono = paciente.contactoTelefono
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    paciente.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                paciente.fechaNacimiento?.let {
                    Text(
                        ReglasPaciente.textoEdad(it, hoy),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatoDestacado(
                    etiqueta = "Grupo sanguíneo",
                    valor = paciente.grupoSanguineo?.etiqueta ?: "No registrado",
                    modifier = Modifier.weight(1f)
                )
                DatoDestacado(
                    etiqueta = "Previsión",
                    valor = paciente.prevision?.etiqueta ?: "No registrada",
                    modifier = Modifier.weight(1f)
                )
            }
            //las alergias van en rojo, es lo primero que necesita saber quien atiende
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Alergias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                //vacio quiere decir que no se sabe, que es distinto de "Ninguna"
                Text(
                    paciente.alergias ?: "No registradas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Column {
                Etiqueta("Enfermedades")
                Text(paciente.enfermedades ?: "No registradas", style = MaterialTheme.typography.bodyLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Etiqueta("Medicamentos")
                if (datos.medicamentos.isEmpty()) {
                    Text("No registrados", style = MaterialTheme.typography.bodyLarge)
                }
                //@Relation no asegura el orden, se ordenan aqui por nombre
                datos.medicamentos.sortedBy { it.nombre.lowercase() }.forEach {
                    NombreYDosis(it, MaterialTheme.typography.bodyLarge)
                }
            }
            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta("Contacto de emergencia")
                if (contacto != null && telefono != null) {
                    Column {
                        Text(
                            contacto,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(telefono, style = MaterialTheme.typography.bodyLarge)
                    }
                    Button(
                        onClick = { onLlamar(telefono) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        //el primer nombre basta y el boton no queda tan largo
                        Text("Llamar a ${contacto.substringBefore(" ")}")
                    }
                } else {
                    Text("No registrado", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

//grupo sanguineo y prevision en cuadros grandes, se leen de un vistazo
@Composable
private fun DatoDestacado(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Etiqueta(etiqueta)
        Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}