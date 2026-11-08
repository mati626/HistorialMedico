package com.example.historialmedico.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.ReglasPastillas
import com.example.historialmedico.util.Documentos

//pantalla de un medicamento, eliminar esta aqui y no en la lista para no borrar por error
@Composable
fun MedicamentoDetalle(
    medicamento: Medicamento,
    horas: List<Int>,
    onReponer: (pastillas: Int) -> Unit,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current
    var reponiendo by rememberSaveable { mutableStateOf(false) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "${medicamento.nombre} ${medicamento.dosis}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        BloqueDetalle("Recordatorios") {
            Text(textoHorarios(horas), style = MaterialTheme.typography.bodyLarge)
        }
        //solo los que tienen recordatorio llevan la cuenta, porque solo esas tomas se marcan en Hoy
        if (horas.isNotEmpty()) {
            BloqueDetalle("Pastillas") {
                val cantidad = medicamento.cantidad
                if (cantidad == null) {
                    Text("No se lleva la cuenta.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    TextoPastillas(cantidad, medicamento.unidadesPorToma, horas.size)
                    val unidades = medicamento.unidadesPorToma
                    Text(
                        "$unidades ${if (unidades == 1) "pastilla" else "pastillas"} por toma",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { reponiendo = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(if (cantidad == null) "Contar pastillas" else "Reponer")
                }
            }
        }
        medicamento.documentoUri?.let { uri ->
            OutlinedButton(
                onClick = { Documentos.abrir(context, uri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Ver receta")
            }
        }
        BotonEliminar("Eliminar medicamento") { confirmarEliminar = true }
    }
    if (reponiendo) {
        ReponerDialog(
            medicamento = medicamento,
            onConfirmar = { pastillas ->
                onReponer(pastillas)
                reponiendo = false
            },
            onCancelar = { reponiendo = false }
        )
    }

    if (confirmarEliminar) {
        ConfirmarEliminar(
            titulo = "¿Eliminar ${medicamento.nombre}?",
            texto = if (medicamento.documentoUri != null) {
                "También se borran sus recordatorios y la receta escaneada. No se puede deshacer."
            } else {
                "También se borran sus recordatorios. No se puede deshacer."
            },
            onConfirmar = {
                confirmarEliminar = false
                onEliminar()
            },
            onCancelar = { confirmarEliminar = false }
        )
    }
}

@Composable
private fun ReponerDialog(
    medicamento: Medicamento,
    onConfirmar: (pastillas: Int) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    //la primera vez se cuentan las que tiene, despues se suman las que compro
    val contando = medicamento.cantidad == null

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                if (contando) "Contar pastillas de ${medicamento.nombre}"
                else "Reponer ${medicamento.nombre}"
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) texto = it },
                    label = { Text(if (contando) "Pastillas que tiene" else "Pastillas que compró") },
                    isError = error != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                val pastillas = texto.toIntOrNull()
                val errorCantidad = ReglasPastillas.validarCantidad(
                    pastillas,
                    actual = medicamento.cantidad ?: 0
                )
                when {
                    errorCantidad != null -> error = errorCantidad
                    pastillas != null -> onConfirmar(pastillas)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}