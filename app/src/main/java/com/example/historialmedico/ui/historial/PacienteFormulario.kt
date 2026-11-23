package com.example.historialmedico.ui.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.EstadoCivil
import com.example.historialmedico.data.GrupoSanguineo
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.Prevision
import com.example.historialmedico.data.ReglasPaciente
import com.example.historialmedico.data.Sexo
import com.example.historialmedico.ui.components.SelectorFechaDialog
import com.example.historialmedico.ui.components.SelectorOpcion
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto

//sirve para agregar (inicial null) y para editar, guardar lo hace la pantalla que lo abre
@Composable
fun PacienteFormulario(
    perfilId: Long,
    inicial: Paciente?,
    pacientes: List<Paciente>,
    onGuardar: (Paciente) -> Unit
) {
    //rememberSaveable: lo escrito se mantiene al cambiar el tema o de pestaña
    var nombre by rememberSaveable { mutableStateOf(inicial?.nombre.orEmpty()) }
    //una relacion antigua que no esta en la lista hay que volver a elegirla
    var relacion by rememberSaveable {
        mutableStateOf(inicial?.relacion?.takeIf { it in ReglasPaciente.RELACIONES })
    }
    var fechaNacimiento by rememberSaveable { mutableStateOf(inicial?.fechaNacimiento) }
    var sexo by rememberSaveable { mutableStateOf(inicial?.sexo) }
    var estadoCivil by rememberSaveable { mutableStateOf(inicial?.estadoCivil) }
    var prevision by rememberSaveable { mutableStateOf(inicial?.prevision) }
    var grupoSanguineo by rememberSaveable { mutableStateOf(inicial?.grupoSanguineo) }
    var alergias by rememberSaveable { mutableStateOf(inicial?.alergias.orEmpty()) }
    var enfermedades by rememberSaveable { mutableStateOf(inicial?.enfermedades.orEmpty()) }
    var contactoNombre by rememberSaveable { mutableStateOf(inicial?.contactoNombre.orEmpty()) }
    var contactoTelefono by rememberSaveable { mutableStateOf(inicial?.contactoTelefono.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    //evita guardar dos veces si se toca Guardar dos veces seguidas
    var guardado by remember { mutableStateOf(false) }

    fun guardar() {
        if (guardado) return
        //al editar se parte del paciente guardado, copy mantiene el id y lo que este formulario no edita, como mostrarEmergencia
        val base = inicial ?: Paciente(perfilId = perfilId, nombre = "", relacion = "")
        val paciente = base.copy(
            nombre = ReglasTexto.normalizar(nombre),
            //vacia si no se eligio, asi validar pide elegirla
            relacion = relacion.orEmpty(),
            fechaNacimiento = fechaNacimiento,
            sexo = sexo,
            estadoCivil = estadoCivil,
            prevision = prevision,
            grupoSanguineo = grupoSanguineo,
            alergias = ReglasTexto.opcional(alergias),
            enfermedades = ReglasTexto.opcional(enfermedades),
            contactoNombre = ReglasTexto.opcional(contactoNombre),
            contactoTelefono = ReglasTexto.opcional(contactoTelefono)
        )
        error = ReglasPaciente.validar(paciente, pacientes, Fechas.hoyIso())
        if (error != null) return
        guardado = true
        onGuardar(paciente)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { if (it.length <= CampoTexto.NOMBRE_PERSONA.maximo) nombre = it },
            label = { Text("Nombre") },
            supportingText = { Text("${nombre.length}/${CampoTexto.NOMBRE_PERSONA.maximo}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        SelectorOpcion(
            etiqueta = "Relación",
            seleccion = relacion,
            opciones = ReglasPaciente.RELACIONES,
            texto = { it },
            onElegir = { relacion = it }
        )
        OutlinedButton(
            onClick = { mostrarSelectorFecha = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                fechaNacimiento?.let { "Fecha de nacimiento: ${Fechas.isoAVisible(it)}" }
                    ?: "Elegir fecha de nacimiento"
            )
        }
        SelectorOpcion(
            etiqueta = "Sexo",
            seleccion = sexo,
            opciones = Sexo.entries,
            texto = { it.etiqueta },
            onElegir = { sexo = it }
        )
        SelectorOpcion(
            etiqueta = "Estado civil",
            seleccion = estadoCivil,
            opciones = EstadoCivil.entries,
            texto = { it.etiqueta },
            onElegir = { estadoCivil = it }
        )
        SelectorOpcion(
            etiqueta = "Previsión",
            seleccion = prevision,
            opciones = Prevision.entries,
            texto = { it.etiqueta },
            onElegir = { prevision = it }
        )
        SelectorOpcion(
            etiqueta = "Grupo sanguíneo",
            seleccion = grupoSanguineo,
            opciones = GrupoSanguineo.entries,
            texto = { it.etiqueta },
            onElegir = { grupoSanguineo = it }
        )
        SeccionEmergencia(
            alergias = alergias,
            onAlergias = { alergias = it },
            enfermedades = enfermedades,
            onEnfermedades = { enfermedades = it },
            contactoNombre = contactoNombre,
            onContactoNombre = { contactoNombre = it },
            contactoTelefono = contactoTelefono,
            onContactoTelefono = { contactoTelefono = it }
        )
        //el error va justo sobre el boton, donde se esta mirando al tocar Guardar
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = { guardar() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text("Guardar paciente")
        }
    }

    if (mostrarSelectorFecha) {
        SelectorFechaDialog(
            onConfirmar = {
                fechaNacimiento = Fechas.utcMillisAIso(it)
                mostrarSelectorFecha = false
            },
            onCancelar = { mostrarSelectorFecha = false },
            inicial = fechaNacimiento?.let { Fechas.isoAUtcMillis(it) }
        )
    }
}

//datos opcionales para emergencias, mostrarlos sin PIN se activa aparte desde la ficha
@Composable
private fun SeccionEmergencia(
    alergias: String,
    onAlergias: (String) -> Unit,
    enfermedades: String,
    onEnfermedades: (String) -> Unit,
    contactoNombre: String,
    onContactoNombre: (String) -> Unit,
    contactoTelefono: String,
    onContactoTelefono: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        "Para emergencias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Opcional. Ayudan a quien atienda a esta persona en una emergencia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedTextField(
                value = alergias,
                onValueChange = { if (it.length <= CampoTexto.ALERGIAS.maximo) onAlergias(it) },
                label = { Text("Alergias") },
                supportingText = {
                    Row {
                        //vacio quiere decir que no se sabe, por eso se pide escribir Ninguna
                        Text("Si no tiene, escriba \"Ninguna\"", modifier = Modifier.weight(1f))
                        Text("${alergias.length}/${CampoTexto.ALERGIAS.maximo}")
                    }
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = enfermedades,
                onValueChange = { if (it.length <= CampoTexto.ENFERMEDADES.maximo) onEnfermedades(it) },
                label = { Text("Enfermedades") },
                supportingText = { Text("${enfermedades.length}/${CampoTexto.ENFERMEDADES.maximo}") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Contacto de emergencia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = contactoNombre,
                onValueChange = { if (it.length <= CampoTexto.CONTACTO.maximo) onContactoNombre(it) },
                label = { Text("Nombre del contacto") },
                supportingText = { Text("${contactoNombre.length}/${CampoTexto.CONTACTO.maximo}") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contactoTelefono,
                onValueChange = onContactoTelefono,
                label = { Text("Teléfono del contacto") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                supportingText = { Text("Ejemplo: +56 9 1234 5678") },
                singleLine = true,
                //abre el teclado de numeros, con + y espacio
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Mostrarlos en la pantalla de bloqueo se activa desde la ficha, con el interruptor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}