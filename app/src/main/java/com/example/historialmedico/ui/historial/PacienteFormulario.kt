package com.example.historialmedico.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
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
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    //evita guardar dos veces si se toca Guardar dos veces seguidas
    var guardado by remember { mutableStateOf(false) }

    fun guardar() {
        if (guardado) return
        val paciente = Paciente(
            //al editar se mantiene el id, asi Room actualiza el mismo paciente
            id = inicial?.id ?: 0,
            perfilId = perfilId,
            nombre = ReglasTexto.normalizar(nombre),
            //vacia si no se eligio, asi validar pide elegirla
            relacion = relacion.orEmpty(),
            fechaNacimiento = fechaNacimiento,
            sexo = sexo,
            estadoCivil = estadoCivil,
            prevision = prevision,
            grupoSanguineo = grupoSanguineo
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