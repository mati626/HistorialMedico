package com.example.historialmedico.ui.historial

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PresionArterial
import com.example.historialmedico.data.ReglasPresion
import com.example.historialmedico.ui.components.SelectorFechaHoraDialog
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import com.example.historialmedico.util.LectorTexto
import com.example.historialmedico.util.ReglasOcr
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//formulario aparte, guardar lo hace la lista que sigue abierta al cerrarlo
@Composable
fun PresionFormulario(
    paciente: Paciente,
    onGuardar: (medicion: PresionArterial) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sistolica by rememberSaveable { mutableStateOf("") }
    var diastolica by rememberSaveable { mutableStateOf("") }
    var pulso by rememberSaveable { mutableStateOf("") }
    //null = ahora, la hora se toma al guardar
    var fechaHora by rememberSaveable { mutableStateOf<Long?>(null) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarSelectorFechaHora by remember { mutableStateOf(false) }
    //evita guardar dos veces si se toca Guardar dos veces seguidas
    var guardado by remember { mutableStateOf(false) }
    val scanner = remember { Documentos.crearEscaner() }

    //reemplaza los 3 campos porque son una misma medicion, el usuario confirma al guardar
    suspend fun prellenarDesde(uri: Uri) {
        error = null
        aviso = "Leyendo el texto..."
        val leida = LectorTexto.leer(context, uri)?.let { ReglasOcr.leerPresion(it) }
        if (leida == null) {
            aviso = "No se pudo leer la imagen; ingrese los valores a mano"
            return
        }
        val sistolicaLeida = leida.sistolica
        val diastolicaLeida = leida.diastolica
        if (sistolicaLeida == null || diastolicaLeida == null) {
            aviso = "No se encontró la presión; ingrésela a mano"
            return
        }
        sistolica = sistolicaLeida.toString()
        diastolica = diastolicaLeida.toString()
        pulso = leida.pulso?.toString().orEmpty()
        aviso = "Revise los valores leídos antes de guardar"
    }

    //la foto solo se lee, no se guarda y se borra del cache
    val presionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val pagina = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
                ?.pages?.firstOrNull()
            if (pagina != null) {
                scope.launch {
                    try {
                        prellenarDesde(pagina.imageUri)
                    } finally {
                        withContext(NonCancellable) { Documentos.borrar(pagina.imageUri.toString()) }
                    }
                }
            }
        }
    }

    fun guardar() {
        if (guardado) return
        val sistolicaNum = sistolica.toIntOrNull()
        val diastolicaNum = diastolica.toIntOrNull()
        val pulsoNum = pulso.toIntOrNull()
        val ahora = System.currentTimeMillis()
        val momento = fechaHora ?: ahora
        error = ReglasPresion.validar(sistolicaNum, diastolicaNum, pulsoNum)
            ?: if (momento > ahora) "La medición no puede tener una fecha futura" else null
        //validar ya reviso que no sean null, la condicion es para que Kotlin lo sepa
        if (error != null || sistolicaNum == null || diastolicaNum == null) return
        guardado = true
        onGuardar(
            PresionArterial(
                pacienteId = paciente.id,
                sistolica = sistolicaNum,
                diastolica = diastolicaNum,
                pulso = pulsoNum,
                fechaHora = momento
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Puede escanear un papel con la presión anotada, por ejemplo 120/80.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = {
                Documentos.abrirEscaner(context, scanner, presionLauncher) {
                    error = "No se pudo abrir el escáner"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Escanear presión")
        }
        aviso?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CampoNumero(sistolica, "Sistólica (la alta)") { sistolica = it }
        CampoNumero(diastolica, "Diastólica (la baja)") { diastolica = it }
        CampoNumero(pulso, "Pulso (opcional)") { pulso = it }
        OutlinedButton(
            onClick = { mostrarSelectorFechaHora = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                fechaHora?.let { "Fecha y hora: ${Fechas.fechaHoraVisible(it)}" }
                    ?: "Fecha y hora: ahora (tocar para cambiar)"
            )
        }
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
            Text("Guardar medición")
        }
    }

    if (mostrarSelectorFechaHora) {
        SelectorFechaHoraDialog(
            onConfirmar = {
                fechaHora = it
                mostrarSelectorFechaHora = false
            },
            onCancelar = { mostrarSelectorFechaHora = false }
        )
    }
}

//campo de hasta 3 digitos con teclado numerico
@Composable
private fun CampoNumero(valor: String, etiqueta: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) onCambio(it) },
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}