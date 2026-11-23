package com.example.historialmedico.ui.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.ReglasPaciente
import com.example.historialmedico.util.Fechas

//pestaña Ficha, editar y eliminar estan aqui y no en la lista para no borrar por error
@Composable
fun PestanaFicha(
    paciente: Paciente,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onMostrarEmergencia: (Boolean) -> Unit
) {
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }
    var confirmarEmergencia by rememberSaveable { mutableStateOf(false) }
    //se renueva al volver a la app, asi la edad cambia el dia del cumpleaños
    val hoy = Fechas.millisAIso(rememberAhora())
    val completa = ReglasPaciente.fichaCompleta(paciente)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ficha", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (!completa) {
            Text(
                "Faltan datos de la ficha. Complételos con el botón de abajo.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        BloqueDetalle("Datos personales") {
            DatoFicha("Relación", paciente.relacion)
            DatoFicha(
                "Fecha de nacimiento",
                paciente.fechaNacimiento?.let {
                    "${Fechas.isoAVisible(it)} (${ReglasPaciente.textoEdad(it, hoy)})"
                }
            )
            DatoFicha("Sexo", paciente.sexo?.etiqueta)
            DatoFicha("Estado civil", paciente.estadoCivil?.etiqueta)
        }
        BloqueDetalle("Salud") {
            DatoFicha("Previsión", paciente.prevision?.etiqueta)
            DatoFicha("Grupo sanguíneo", paciente.grupoSanguineo?.etiqueta)
            DatoFicha("Alergias", paciente.alergias, siNoHay = "No registradas")
            DatoFicha("Enfermedades", paciente.enfermedades, siNoHay = "No registradas")
        }
        BloqueDetalle("Contacto de emergencia") {
            DatoFicha("Nombre", paciente.contactoNombre, siNoHay = "No registrado")
            DatoFicha("Teléfono", paciente.contactoTelefono, siNoHay = "No registrado")
        }
        TarjetaMostrarEmergencia(
            nombre = paciente.nombre,
            activa = paciente.mostrarEmergencia,
            onCambiar = { activar ->
                //activar deja ver datos sin PIN y pide confirmacion, desactivar los protege y es inmediato
                if (activar) confirmarEmergencia = true else onMostrarEmergencia(false)
            }
        )
        Button(
            onClick = onEditar,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(if (completa) "Editar datos" else "Completar ficha")
        }
        BotonEliminar("Eliminar paciente") { confirmarEliminar = true }
    }

    if (confirmarEliminar) {
        ConfirmarEliminar(
            titulo = "¿Eliminar a ${paciente.nombre}?",
            texto = "También se eliminarán sus medicamentos, horas médicas, exámenes, mediciones de presión y documentos. No se puede deshacer.",
            onConfirmar = {
                confirmarEliminar = false
                onEliminar()
            },
            onCancelar = { confirmarEliminar = false }
        )
    }

    if (confirmarEmergencia) {
        ConfirmarEmergencia(
            nombre = paciente.nombre,
            onConfirmar = {
                confirmarEmergencia = false
                onMostrarEmergencia(true)
            },
            onCancelar = { confirmarEmergencia = false }
        )
    }
}

//un dato de la ficha, si falta lo dice en texto y no solo con el color
//los opcionales dicen que no estan registrados, sin marcarlos como error
@Composable
private fun DatoFicha(etiqueta: String, valor: String?, siNoHay: String? = null) {
    Column {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor ?: siNoHay ?: "Falta completar",
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                valor != null -> MaterialTheme.colorScheme.onSurface
                siNoHay != null -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.error
            }
        )
    }
}

//el interruptor va en la ficha y no en el formulario, asi siempre se ve si los datos se muestran sin PIN
@Composable
private fun TarjetaMostrarEmergencia(nombre: String, activa: Boolean, onCambiar: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //toda la fila se puede tocar y el lector de pantalla la lee como un interruptor con su texto
            Row(
                modifier = Modifier.toggleable(
                    value = activa,
                    role = Role.Switch,
                    onValueChange = onCambiar
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Mostrar en la pantalla de bloqueo",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Así, quien ayude a $nombre podrá ver estos datos sin PIN.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                //sin onCheckedChange: el toque lo recibe la fila entera
                Switch(checked = activa, onCheckedChange = null)
            }
            HorizontalDivider()
            Text(
                "Se muestra: nombre, edad, grupo sanguíneo, previsión, alergias, enfermedades, medicamentos y contacto. No se muestran exámenes, horas médicas, presión ni documentos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

//se pide solo al activar, porque es lo que deja ver datos de salud sin PIN
@Composable
private fun ConfirmarEmergencia(nombre: String, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("¿Mostrar los datos de $nombre sin PIN?") },
        text = {
            //con letra grande puede no caber, asi se puede deslizar
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Cualquier persona que tenga este teléfono podrá verlos desde la pantalla de bloqueo:")
                listOf(
                    "Nombre y edad",
                    "Grupo sanguíneo y previsión",
                    "Alergias y enfermedades",
                    "Medicamentos que toma",
                    "Contacto de emergencia"
                ).forEach { dato ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dato)
                    }
                }
                Text(
                    "No se muestran exámenes, horas médicas, presión ni documentos. Puede desactivarlo cuando quiera.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Sí, mostrar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}