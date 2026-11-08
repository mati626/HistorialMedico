package com.example.historialmedico.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.EstadoExamen
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.ReglasExamen
import com.example.historialmedico.ui.components.SelectorFechaDialog
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto

//pantalla de un examen, eliminar esta aqui y no en la lista para no borrar por error
@Composable
fun ExamenDetalle(
    examen: Examen,
    consulta: HoraMedica?,
    onMarcarRealizado: (fecha: String, resultado: String?) -> Unit,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current
    var marcando by rememberSaveable { mutableStateOf(false) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            examen.tipo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        BloqueDetalle("Estado") {
            Text(examen.estado.etiqueta, style = MaterialTheme.typography.bodyLarge)
        }
        BloqueDetalle("Fecha") {
            Text(
                examen.fecha?.let { Fechas.isoAVisible(it) } ?: "Sin fecha",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        consulta?.let {
            BloqueDetalle("Solicitado en") {
                Text(ReglasExamen.textoConsulta(it), style = MaterialTheme.typography.bodyLarge)
            }
        }
        examen.resultado?.let {
            BloqueDetalle("Resultado") {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (examen.estado != EstadoExamen.REALIZADO) {
            Button(
                onClick = { marcando = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text("Marcar como realizado")
            }
        }
        examen.documentoUri?.let { uri ->
            OutlinedButton(
                onClick = { Documentos.abrir(context, uri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Ver documento")
            }
        }
        BotonEliminar("Eliminar examen") { confirmarEliminar = true }
    }

    if (marcando) {
        MarcarRealizadoDialog(
            examen = examen,
            fechaConsulta = consulta?.let { Fechas.millisAIso(it.fechaHora) },
            onConfirmar = { fecha, resultado ->
                onMarcarRealizado(fecha, resultado)
                marcando = false
            },
            onCancelar = { marcando = false }
        )
    }

    if (confirmarEliminar) {
        ConfirmarEliminar(
            titulo = "¿Eliminar el examen ${examen.tipo}?",
            texto = if (examen.documentoUri != null) {
                "También se borra el documento escaneado. No se puede deshacer."
            } else {
                "No se puede deshacer."
            },
            onConfirmar = {
                confirmarEliminar = false
                onEliminar()
            },
            onCancelar = { confirmarEliminar = false }
        )
    }
}

//la fecha agendada se ofrece solo si ya paso, un examen realizado no puede tener fecha futura
@Composable
private fun MarcarRealizadoDialog(
    examen: Examen,
    fechaConsulta: String?,
    onConfirmar: (fecha: String, resultado: String?) -> Unit,
    onCancelar: () -> Unit
) {
    var fecha by remember {
        mutableStateOf(examen.fecha?.takeIf {
            ReglasExamen.validarFecha(true, it, Fechas.hoyIso()) == null
        })
    }
    var resultado by remember { mutableStateOf("") }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Marcar como realizado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(examen.tipo)
                OutlinedButton(
                    onClick = { mostrarSelectorFecha = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        fecha?.let { "Fecha: ${Fechas.isoAVisible(it)}" }
                            ?: "Elegir fecha en que se realizó"
                    )
                }
                OutlinedTextField(
                    value = resultado,
                    onValueChange = {
                        if (it.length <= CampoTexto.RESULTADO.maximo) resultado = it
                    },
                    label = { Text("Resultado (opcional)") },
                    supportingText = {
                        Text("${resultado.length}/${CampoTexto.RESULTADO.maximo}")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val fechaElegida = fecha
                error = ReglasExamen.validarFecha(true, fechaElegida, Fechas.hoyIso())
                    ?: ReglasExamen.validarConsulta(fechaElegida, fechaConsulta)
                            ?: ReglasTexto.validar(
                        resultado,
                        CampoTexto.RESULTADO,
                        obligatorio = false
                    )
                if (error == null && fechaElegida != null) {
                    onConfirmar(fechaElegida, ReglasTexto.normalizar(resultado).ifBlank { null })
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )

    if (mostrarSelectorFecha) {
        SelectorFechaDialog(
            onConfirmar = {
                fecha = Fechas.utcMillisAIso(it)
                mostrarSelectorFecha = false
            },
            onCancelar = { mostrarSelectorFecha = false }
        )
    }
}