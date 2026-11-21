package com.example.historialmedico.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    onEliminar: () -> Unit
) {
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }
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
        }
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
}

//un dato de la ficha, si falta lo dice en texto y no solo con el color
@Composable
private fun DatoFicha(etiqueta: String, valor: String?) {
    Column {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor ?: "Falta completar",
            style = MaterialTheme.typography.bodyLarge,
            color = if (valor == null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}