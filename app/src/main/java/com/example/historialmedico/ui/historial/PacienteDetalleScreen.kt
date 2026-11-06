package com.example.historialmedico.ui.historial

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.R
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.ControlTomaDao
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PresionArterialDao
import com.example.historialmedico.export.PdfExporter
import com.example.historialmedico.security.SalidaInterna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun PacienteDetalleScreen(
    paciente: Paciente,
    medicamentoDao: MedicamentoDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao,
    alarmaDao: AlarmaDao,
    controlTomaDao: ControlTomaDao,
    presionArterialDao: PresionArterialDao,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(Pestana.HISTORIAL) }
    //null = se ven las tarjetas; si no, la lista abierta
    var seccion by rememberSaveable { mutableStateOf<Seccion?>(null) }

    BackHandler {
        //atras primero cierra la lista abierta y después vuelve a Pacientes
        if (pestana == Pestana.HISTORIAL && seccion != null) seccion = null else onBack()
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == Pestana.HISTORIAL,
                    onClick = {
                        //tocar Historial estando en él vuelve a las tarjetas
                        if (pestana == Pestana.HISTORIAL) seccion = null
                        pestana = Pestana.HISTORIAL
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = pestana == Pestana.COMPARTIR,
                    onClick = { pestana = Pestana.COMPARTIR },
                    icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                    label = { Text("Compartir") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            val abierta = seccion
            when {
                pestana == Pestana.COMPARTIR -> {
                    EncabezadoPaciente(paciente, onCambiar = onBack)
                    PestanaCompartir(
                        paciente,
                        medicamentoDao,
                        horaMedicaDao,
                        examenDao,
                        presionArterialDao
                    )
                }

                abierta != null -> {
                    TextButton(onClick = { seccion = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Volver")
                    }
                    Text(
                        abierta.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        when (abierta) {
                            Seccion.MEDICAMENTOS -> MedicamentosSection(
                                dao = medicamentoDao,
                                alarmaDao = alarmaDao,
                                controlTomaDao = controlTomaDao,
                                paciente = paciente
                            )

                            Seccion.HORAS_MEDICAS -> HorasMedicasSection(
                                dao = horaMedicaDao,
                                alarmaDao = alarmaDao,
                                paciente = paciente
                            )

                            Seccion.EXAMENES -> ExamenesSection(
                                dao = examenDao,
                                horaMedicaDao = horaMedicaDao,
                                paciente = paciente
                            )

                            Seccion.PRESION -> PresionArterialSection(
                                dao = presionArterialDao,
                                paciente = paciente
                            )
                        }
                    }
                }

                else -> {
                    EncabezadoPaciente(paciente, onCambiar = onBack)
                    TarjetasHistorial(onAbrir = { seccion = it })
                }
            }
        }
    }
}

@Composable
private fun EncabezadoPaciente(paciente: Paciente, onCambiar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                paciente.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                paciente.relacion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onCambiar) { Text("Cambiar") }
    }
}

@Composable
private fun TarjetasHistorial(onAbrir: (Seccion) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Historial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        //4 tarjetas en 2 filas de 2
        Seccion.entries.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { s ->
                    Card(
                        onClick = { onAbrir(s) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .heightIn(min = 140.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painterResource(s.icono),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                s.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PestanaCompartir(
    paciente: Paciente,
    medicamentoDao: MedicamentoDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao,
    presionArterialDao: PresionArterialDao
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var exportando by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Compartir", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Crea un PDF con el historial para mostrárselo o enviárselo a su médico.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Incluye medicamentos, horas médicas, exámenes, presión y documentos escaneados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Button(
            onClick = {
                exportando = true
                scope.launch {
                    val uri = try {
                        withContext(Dispatchers.IO) {
                            val medicamentos = medicamentoDao.getByPaciente(paciente.id).first()
                            val horasMedicas = horaMedicaDao.getByPaciente(paciente.id).first()
                            val examenes = examenDao.getByPaciente(paciente.id).first()
                            val presiones =
                                presionArterialDao.getByPaciente(paciente.id).first()
                            PdfExporter.generarPdf(
                                context,
                                paciente,
                                medicamentos,
                                horasMedicas,
                                examenes,
                                presiones
                            )
                        }
                    } catch (e: IOException) {
                        null
                    } catch (e: OutOfMemoryError) {
                        null
                    }
                    exportando = false
                    if (uri == null) {
                        Toast.makeText(context, "No se pudo generar el PDF", Toast.LENGTH_LONG)
                            .show()
                        return@launch
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Exportar Historial"))
                    SalidaInterna.marcar()
                }
            },
            enabled = !exportando,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (exportando) "Generando PDF..." else "Compartir historial en PDF")
        }
    }
}

//pestaña de la barra inferior
private enum class Pestana { HISTORIAL, COMPARTIR }

//cada tarjeta del historial abre una de estas listas
private enum class Seccion(val titulo: String, @DrawableRes val icono: Int) {
    MEDICAMENTOS("Medicamentos", R.drawable.ic_medicamento),
    HORAS_MEDICAS("Horas médicas", R.drawable.ic_hora_medica),
    EXAMENES("Exámenes", R.drawable.ic_examen),
    PRESION("Presión", R.drawable.ic_presion)
}
