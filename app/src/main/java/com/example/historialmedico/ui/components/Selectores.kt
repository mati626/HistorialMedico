package com.example.historialmedico.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.historialmedico.util.Fechas
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFechaDialog(
    onConfirmar: (Long) -> Unit,
    onCancelar: () -> Unit,
    inicial: Long? = null
) {
    val estado = rememberDatePickerState(initialSelectedDateMillis = inicial)
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = { estado.selectedDateMillis?.let(onConfirmar) },
                enabled = estado.selectedDateMillis != null
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estado)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorHoraDialog(
    onConfirmar: (hora: Int, minuto: Int) -> Unit,
    onCancelar: () -> Unit
) {
    val ahora = remember { Calendar.getInstance() }
    val estado = rememberTimePickerState(
        initialHour = ahora.get(Calendar.HOUR_OF_DAY),
        initialMinute = ahora.get(Calendar.MINUTE),
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(onClick = { onConfirmar(estado.hour, estado.minute) })
            {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        },
        text = { TimePicker(state = estado) }
    )
}

@Composable
fun SelectorFechaHoraDialog(
    onConfirmar: (Long) -> Unit,
    onCancelar: () -> Unit,

) {
    var fechaUtcMillis by remember { mutableStateOf<Long?>(null) }
    val fecha = fechaUtcMillis
    if (fecha == null) {
        SelectorFechaDialog(
            onConfirmar = { fechaUtcMillis = it },
            onCancelar = onCancelar
        )
    } else {
        SelectorHoraDialog(
            onConfirmar = { hora, minuto -> onConfirmar(Fechas.combinar(fecha, hora, minuto)) },
            onCancelar = onCancelar
        )
    }
}

//boton que abre una lista para elegir una opcion, T es el tipo de la opcion (texto, Sexo, etc)
@Composable
fun <T> SelectorOpcion(
    etiqueta: String,
    seleccion: T?,
    opciones: List<T>,
    texto: (T) -> String,
    onElegir: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { abierto = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(seleccion?.let { "$etiqueta: ${texto(it)}" } ?: "Elegir ${etiqueta.lowercase()}")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(texto(opcion)) },
                    onClick = {
                        onElegir(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}