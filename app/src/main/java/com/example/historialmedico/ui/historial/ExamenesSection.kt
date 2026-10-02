package com.example.historialmedico.ui.historial

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.Perfil
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ExamenesSection(dao: ExamenDao,perfil: Perfil){
    val examenes by dao.getByPerfil(perfil.id).collectAsState(initial = emptyList())
    var tipo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var examenAEliminar by remember { mutableStateOf<Examen?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as Activity
    var documentoUri by remember { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode== Activity.RESULT_OK){
            val resultado = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val paginas = resultado?.pages
            if(!paginas.isNullOrEmpty()){
                documentoUri=paginas[0].imageUri.toString()
            }
        }
    }

    val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()

    val scanner = GmsDocumentScanning.getClient(scannerOptions)



    Column(modifier =
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            "Examenes de ${perfil.nombre}",
            style=MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        examenes.forEach { examen ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${examen.tipo} - ${examen.fecha} (${examen.resultado})",
                        modifier = Modifier.weight(1f)
                    )
                    if (examen.documentoUri != null) {
                        TextButton(onClick = {
                            val archivo = File(Uri.parse(examen.documentoUri).path!!)
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                archivo
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Ver")
                        }
                    }
                    IconButton(onClick = { examenAEliminar = examen }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar examen"
                        )
                    }
                }
            }
        }
        examenAEliminar?.let { examen ->
            AlertDialog(
                onDismissRequest = {examenAEliminar=null},
                title = {Text("Eliminar examen")},
                text = {Text("Seguro que quiere eliminar este examen?")},
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(examen) }
                        examenAEliminar=null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton={
                    TextButton(onClick = {examenAEliminar=null}) {
                        Text("Cancelar")
                    }
                }
            )
        }

        OutlinedTextField(
            value=tipo,
            onValueChange = {tipo=it},
            label = {Text("Tipo de Examen")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value=fecha,
            onValueChange = {fecha=it},
            label = {Text("Fecha (ej: 18/09/2026)")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = resultado,
            onValueChange = {resultado=it},
            label = {Text("Resultado")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender -> scannerLauncher
                    .launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener {
                    error="No se logro abrir el escaner"
                }
        }) {
            Text(if(documentoUri==null)"Escanear Documento" else "Documento escaneado (volver a escanear)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                tipo.isBlank()->error="El tipo de examen es obligatorio"
                fecha.isBlank()->error="La fecha es obligatoria"
                resultado.isBlank()->error="El resultado es obligatorio"
                else->{
                    val tipoAGuardar = tipo.trim()
                    val fechaAGuardar = fecha.trim()
                    val resultadoAGuardar = resultado.trim()
                    val documentoAGuardar = documentoUri
                    tipo=""
                    fecha=""
                    resultado=""
                    documentoUri=null
                    error=null
                    scope.launch {
                        dao.insert(
                            Examen(
                                perfilId = perfil.id,
                                tipo = tipoAGuardar,
                                fecha = fechaAGuardar,
                                resultado = resultadoAGuardar,
                                documentoUri = documentoAGuardar
                            )
                        )
                    }
                }
            }
        }) {
            Text("Agregar Examen")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
