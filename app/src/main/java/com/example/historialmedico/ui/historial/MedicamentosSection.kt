package com.example.historialmedico.ui.historial

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Perfil
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun MedicamentosSection (dao: MedicamentoDao,perfil: Perfil) {
    val medicamentos by dao.getByPerfil(perfil.id).collectAsState(initial = emptyList())
    var medicamentoAEliminar by remember{mutableStateOf<Medicamento?>(null)}
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context= LocalContext.current
    val activity= context as Activity
    var documentoUri by remember { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if(activityResult.resultCode== Activity.RESULT_OK){
            val resultado= GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val paginas = resultado?.pages
            if(!paginas.isNullOrEmpty()){
                documentoUri=paginas[0].imageUri.toString()
            }
        }
    }


    val scannerOptions= GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()

    val scanner= GmsDocumentScanning.getClient(scannerOptions)

    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(
            "Medicamentos de ${perfil.nombre}", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        medicamentos.forEach { medicamento ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    if(medicamento.documentoUri!=null){
                        TextButton(onClick = {
                            val archivo= File(Uri.parse(medicamento.documentoUri).path!!)
                            val contentUri= FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",archivo
                            )
                            val intent=Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri,"image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Ver Receta")
                        }
                    }
                    Text(
                        if (medicamento.dosis.isBlank()) medicamento.nombre
                        else "${medicamento.nombre}- ${medicamento.dosis}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { medicamentoAEliminar = medicamento }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar medicamento"
                        )
                    }
                }
            }
        }
        medicamentoAEliminar?.let { medicamento ->
            AlertDialog(
                onDismissRequest = { medicamentoAEliminar = null },
                title = { Text("Eliminar Medicamento") },
                text = { Text("Seguro que quieres eliminar ${medicamento.nombre}?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { dao.delete(medicamento) }
                        medicamentoAEliminar = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        medicamentoAEliminar = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }


        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = dosis,
                onValueChange = { dosis = it },
                label = { Text("Dosis") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier= Modifier.height(8.dp))
        Button(onClick = {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender -> scannerLauncher
                    .launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener {
                    error="No se logro abrir el escaner"
                }
        }) {
            Text(if(documentoUri==null)"Escanear Receta" else "Receta escaneada (volver a escanear)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                nombre.isBlank()->error="El nombre del medicamento es obligatorio"
                dosis.isBlank()->error="La dosis es obligatoria"
                !dosis.any{it.isDigit()}->error="La dosis debe incluir un numero (ej:500mg)"
                medicamentos.size>=MAX_MEDICAMENTOS_POR_PERFIL->error="Este perfil ya tiene el maximo de $MAX_MEDICAMENTOS_POR_PERFIL medicamentos"
                medicamentos.any{it.nombre.trim().equals(nombre.trim(),ignoreCase = true)}->error="Ya existe un medicamento con ese nombre para este perfil"
                else->{
                    val nombreAGuardar=nombre.trim()
                    val dosisAGuardar=dosis.trim()
                    val documentoAGuardar=documentoUri
                    nombre=""
                    dosis=""
                    documentoUri=null
                    error=null
                    scope.launch {
                        dao.insert(
                            Medicamento(
                                perfilId = perfil.id,
                                nombre = nombreAGuardar,
                                dosis=dosisAGuardar,
                                documentoUri = documentoAGuardar
                            )
                        )
                    }
                }
            }
        }) {
            Text("Agregar Medicamento")
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it,color=MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
private const val MAX_MEDICAMENTOS_POR_PERFIL=10