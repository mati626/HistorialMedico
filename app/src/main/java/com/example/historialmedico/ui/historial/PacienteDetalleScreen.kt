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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.historialmedico.R
import com.example.historialmedico.data.Alarma
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.ControlToma
import com.example.historialmedico.data.ControlTomaDao
import com.example.historialmedico.data.EstadoExamen
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PresionArterial
import com.example.historialmedico.data.PresionArterialDao
import com.example.historialmedico.export.PdfExporter
import com.example.historialmedico.security.SalidaInterna
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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
    var pestana by rememberSaveable { mutableStateOf(Pestana.HOY) }
    //null = se ven las tarjetas, si no, la lista abierta
    var seccion by rememberSaveable { mutableStateOf<Seccion?>(null) }
    //dentro de una lista: el registro abierto o el formulario para agregar
    var detalleId by rememberSaveable { mutableStateOf<Long?>(null) }
    var agregando by rememberSaveable { mutableStateOf(false) }
    //guarda lo escrito en un formulario aunque se cambie de pestaña
    val estados = rememberSaveableStateHolder()

    fun abrir(nueva: Seccion?) {
        //al cerrar una lista se borra lo guardado, asi despues no aparece un formulario viejo
        seccion?.let { estados.removeState(it.name) }
        seccion = nueva
        detalleId = null
        agregando = false
    }

    //del formulario o el detalle vuelve a la lista, y de la lista a las tarjetas
    fun volver() {
        when {
            agregando -> agregando = false
            detalleId != null -> detalleId = null
            else -> abrir(null)
        }
    }

    BackHandler {
        //atras primero recorre historial, despues vuelve a Hoy y desde Hoy a Pacientes
        when {
            pestana == Pestana.HISTORIAL && seccion != null -> volver()
            pestana != Pestana.HOY -> pestana = Pestana.HOY
            else -> onBack()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == Pestana.HOY,
                    onClick = { pestana = Pestana.HOY },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Hoy") }
                )
                NavigationBarItem(
                    selected = pestana == Pestana.HISTORIAL,
                    onClick = {
                        //si ya esta en Historial, tocarlo de nuevo vuelve a las tarjetas
                        if (pestana == Pestana.HISTORIAL) abrir(null)
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
                pestana == Pestana.HOY -> {
                    EncabezadoPaciente(paciente, onCambiar = onBack)
                    PestanaHoy(
                        paciente,
                        medicamentoDao,
                        alarmaDao,
                        controlTomaDao,
                        horaMedicaDao,
                        examenDao
                    )
                }

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
                    TextButton(onClick = { volver() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Volver")
                    }
                    //en el detalle no va titulo aqui porque el detalle muestra el nombre del registro
                    val titulo = when {
                        agregando -> abierta.tituloAgregar
                        detalleId != null -> null
                        else -> abierta.titulo
                    }
                    if (titulo != null) {
                        Text(
                            titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        estados.SaveableStateProvider(abierta.name) {
                            when (abierta) {
                                Seccion.MEDICAMENTOS -> MedicamentosSection(
                                    dao = medicamentoDao,
                                    alarmaDao = alarmaDao,
                                    paciente = paciente,
                                    detalleId = detalleId,
                                    agregando = agregando,
                                    onVerDetalle = {
                                        detalleId = it
                                    },

                                    onAgregar = {
                                        agregando = true
                                    },
                                    onVolver = { volver() }
                                )

                                Seccion.HORAS_MEDICAS -> HorasMedicasSection(
                                    dao = horaMedicaDao,
                                    alarmaDao = alarmaDao,
                                    paciente = paciente,
                                    detalleId = detalleId,
                                    agregando = agregando,
                                    onVerDetalle = { detalleId = it },
                                    onAgregar = { agregando = true },
                                    onVolver = { volver() }
                                )

                                Seccion.EXAMENES -> ExamenesSection(
                                    dao = examenDao,
                                    horaMedicaDao = horaMedicaDao,
                                    paciente = paciente,
                                    detalleId = detalleId,
                                    agregando = agregando,
                                    onVerDetalle = { detalleId = it },
                                    onAgregar = { agregando = true },
                                    onVolver = { volver() }
                                )

                                Seccion.PRESION -> PresionArterialSection(
                                    dao = presionArterialDao,
                                    paciente = paciente,
                                    detalleId = detalleId,
                                    agregando = agregando,
                                    onVerDetalle = { detalleId = it },
                                    onAgregar = { agregando = true },
                                    onVolver = { volver() }
                                )
                            }
                        }
                    }
                }

                else -> {
                    EncabezadoPaciente(paciente, onCambiar = onBack)
                    TarjetasHistorial(
                        paciente,
                        medicamentoDao,
                        horaMedicaDao,
                        examenDao,
                        presionArterialDao,
                        onAbrir = { abrir(it) }
                    )
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
private fun TarjetasHistorial(
    paciente: Paciente,
    medicamentoDao: MedicamentoDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao,
    presionArterialDao: PresionArterialDao,
    onAbrir: (Seccion) -> Unit
) {
    val ahora = rememberAhora()
    //null hasta que Room responde, para no mostrar "Ninguno todavía" por un instante
    val medicamentos = remember(paciente.id) { medicamentoDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val horasMedicas = remember(paciente.id) { horaMedicaDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val examenes = remember(paciente.id) { examenDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val presiones = remember(paciente.id) { presionArterialDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Historial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        //4 tarjetas en 2 filas de 2
        Seccion.entries.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { s ->
                    val resumen = when (s) {
                        Seccion.MEDICAMENTOS -> medicamentos?.let { resumenMedicamentos(it.size) }
                        Seccion.HORAS_MEDICAS ->
                            horasMedicas?.let { resumenHorasMedicas(it, ahora) }

                        Seccion.EXAMENES -> examenes?.let { resumenExamenes(it) }
                        Seccion.PRESION -> presiones?.let { resumenPresion(it) }
                    }
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
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    s.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                resumen?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resumenMedicamentos(cantidad: Int): String = when (cantidad) {
    0 -> "Ninguno todavía"
    1 -> "1 registrado"
    else -> "$cantidad registrados"
}

private fun resumenHorasMedicas(horas: List<HoraMedica>, ahora: Long): String {
    val proxima = proximaHora(horas, ahora)
    return when {
        horas.isEmpty() -> "Ninguna todavía"
        proxima == null -> "Sin horas próximas"
        else -> "Próxima: ${Fechas.isoAVisible(Fechas.millisAIso(proxima.fechaHora))}"
    }
}

private fun resumenExamenes(examenes: List<Examen>): String {
    val pendientes = examenes.count { it.estado != EstadoExamen.REALIZADO }
    return when {
        examenes.isEmpty() -> "Ninguno todavía"
        pendientes == 0 -> "Todos realizados"
        pendientes == 1 -> "1 pendiente"
        else -> "$pendientes pendientes"
    }
}

//el DAO las entrega de la mas nueva a la mas antigua
private fun resumenPresion(presiones: List<PresionArterial>): String =
    presiones.firstOrNull()?.let { "Última: ${it.sistolica}/${it.diastolica}" } ?: "Sin mediciones"

//el DAO las entrega ordenadas por fecha, la primera futura es la próxima
private fun proximaHora(horas: List<HoraMedica>, ahora: Long): HoraMedica? =
    horas.firstOrNull { it.fechaHora > ahora }

//el DAO los entrega ordenados por fecha, uno agendado para hoy todavia cuenta como proximo
private fun proximoExamen(examenes: List<Examen>, hoy: String): Examen? =
    examenes.firstOrNull {
        it.estado == EstadoExamen.AGENDADO && it.fecha != null && it.fecha >= hoy
    }

//se renueva al volver a la app, para que "hoy" y "proxima" no queden atrasados
@Composable
internal fun rememberAhora(): Long {
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { ahora = System.currentTimeMillis() }
    return ahora
}

@Composable
private fun PestanaHoy(
    paciente: Paciente,
    medicamentoDao: MedicamentoDao,
    alarmaDao: AlarmaDao,
    controlTomaDao: ControlTomaDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao
) {
    val ahora = rememberAhora()
    val hoy = Fechas.millisAIso(ahora)
    //null hasta que Room responde: si no, "No hay tomas" o una toma sin marcar se ven un instante
    val medicamentos = remember(paciente.id) { medicamentoDao.getByPaciente(paciente.id) }
        .collectAsState(initial = null).value
    val alarmas = remember(paciente.id) { alarmaDao.getDeMedicamentos(paciente.id) }
        .collectAsState(initial = null).value
    val controles = remember(paciente.id, hoy) { controlTomaDao.getDelDia(paciente.id, hoy) }
        .collectAsState(initial = null).value
    val horasMedicas by remember(paciente.id) { horaMedicaDao.getByPaciente(paciente.id) }
        .collectAsState(initial = emptyList())
    val examenes by remember(paciente.id) { examenDao.getByPaciente(paciente.id) }
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val tomasDeHoy = if (medicamentos != null && alarmas != null && controles != null)
        calcularTomasDeHoy(medicamentos, alarmas, controles) else null
    val proxima = proximaHora(horasMedicas, ahora)
    val examen = proximoExamen(examenes, hoy)

    fun marcar(toma: TomaDeHoy, tomada: Boolean) {
        scope.launch {
            //si sale de la pantalla justo despues de tocar, la toma igual se guarda
            withContext(NonCancellable) {
                controlTomaDao.marcarToma(toma.medicamento.id, hoy, toma.hora, tomada)
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (tomasDeHoy != null && tomasDeHoy.isEmpty()) {
            Text(
                "No hay tomas para hoy. Los medicamentos con recordatorio aparecen aquí.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        tomasDeHoy?.forEach { toma ->
            if (toma.tomada) {
                TomaHecha(toma, onDesmarcar = { marcar(toma, false) })
            } else {
                TomaPendiente(toma, onTomar = { marcar(toma, true) })
            }
        }
        proxima?.let {
            TarjetaProximo(
                etiqueta = "Próxima hora médica",
                nombre = it.especialidad,
                fecha = Fechas.fechaHoraVisible(it.fechaHora),
                lugar = it.lugar
            )
        }
        //proximoExamen solo devuelve agendados, que siempre tienen fechas
        if (examen?.fecha != null) {
            TarjetaProximo(
                etiqueta = "Próximo examen",
                nombre = examen.tipo,
                fecha = Fechas.isoAVisible(examen.fecha)
            )
        }
    }
}

@Composable
private fun TomaPendiente(toma: TomaDeHoy, onTomar: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "A las ${Fechas.minutosAHora(toma.hora)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${toma.medicamento.nombre} ${toma.medicamento.dosis}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onTomar,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Marcar como tomada")
            }
        }
    }
}

@Composable
private fun TomaHecha(toma: TomaDeHoy, onDesmarcar: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "A las ${Fechas.minutosAHora(toma.hora)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${toma.medicamento.nombre} ${toma.medicamento.dosis}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                //"Tomada" va en texto y no solo en el color de la tarjeta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Tomada",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            //por si se marco sin querer
            TextButton(onClick = onDesmarcar) { Text("Desmarcar") }
        }
    }
}

//proxima hora medica o examen, la misma tarjeta para los dos
@Composable
private fun TarjetaProximo(etiqueta: String, nombre: String, fecha: String, lugar: String? = null) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(fecha, style = MaterialTheme.typography.bodyLarge)
            lugar?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

//una toma es un recordatorio de un medicamento para hoy
private data class TomaDeHoy(val medicamento: Medicamento, val hora: Int, val tomada: Boolean)

//junta cada recordatorio con su medicamento y lo marcado hoy, primero las pendientes y por hora
private fun calcularTomasDeHoy(
    medicamentos: List<Medicamento>,
    alarmas: List<Alarma>,
    controles: List<ControlToma>
): List<TomaDeHoy> {
    val porId = medicamentos.associateBy { it.id }
    val tomadas = controles.filter { it.tomado }.map { it.medicamentoId to it.hora }.toSet()
    return alarmas.mapNotNull { alarma ->
        //si la alarma llega antes que su medicamento, se salta hasta la siguiente actualizacion
        val medicamento = alarma.medicamentoId?.let { porId[it] } ?: return@mapNotNull null
        val hora = alarma.hora ?: return@mapNotNull null
        TomaDeHoy(medicamento, hora, tomada = (medicamento.id to hora) in tomadas)
    }.sortedWith(compareBy<TomaDeHoy>({ it.tomada }, { it.hora }))
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
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    context.startActivity(Intent.createChooser(intent, "Compartir historial"))
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
private enum class Pestana { HOY, HISTORIAL, COMPARTIR }

//cada tarjeta del historial abre una de estas listas
private enum class Seccion(
    val titulo: String,
    val tituloAgregar: String,
    @DrawableRes val icono: Int
) {
    MEDICAMENTOS("Medicamentos", "Agregar medicamento", R.drawable.ic_medicamento),
    HORAS_MEDICAS("Horas médicas", "Agregar hora médica", R.drawable.ic_hora_medica),
    EXAMENES("Exámenes", "Agregar examen", R.drawable.ic_examen),
    PRESION("Presión", "Agregar medición", R.drawable.ic_presion)
}
