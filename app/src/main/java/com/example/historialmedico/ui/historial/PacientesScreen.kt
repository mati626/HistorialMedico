package com.example.historialmedico.ui.historial

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.AlarmaDao
import com.example.historialmedico.data.ControlTomaDao
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PacienteDao
import com.example.historialmedico.data.Perfil
import com.example.historialmedico.data.PerfilDao
import com.example.historialmedico.data.PresionArterialDao
import com.example.historialmedico.data.ReglasPaciente
import com.example.historialmedico.ui.components.AvisoPrivacidadScreen
import com.example.historialmedico.ui.components.AvisoRecordatorios
import com.example.historialmedico.ui.perfil.CrearPerfilScreen
import com.example.historialmedico.util.Documentos
import com.example.historialmedico.util.Fechas
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PacientesScreen(
    perfilDao: PerfilDao,
    pacienteDao: PacienteDao,
    medicamentoDao: MedicamentoDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao,
    alarmaDao: AlarmaDao,
    controlTomaDao: ControlTomaDao,
    presionArterialDao: PresionArterialDao,
    modifier: Modifier = Modifier,
) {
    var cargando by remember { mutableStateOf(true) }
    var perfil by remember { mutableStateOf<Perfil?>(null) }
    var pacienteId by rememberSaveable { mutableStateOf<Long?>(null) }
    //true mientras se llena el formulario de un paciente nuevo
    var agregando by rememberSaveable { mutableStateOf(false) }
    //sigue vivo al salir del paciente, asi guardar o eliminar no se corta a la mitad
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(perfilDao) {
        perfilDao.getPerfil().collect {
            perfil = it
            cargando = false
        }
    }

    val perfilActual = perfil
    when {
        cargando -> Box(modifier = modifier.fillMaxSize())
        perfilActual == null -> CrearPerfilScreen(
            perfilDao = perfilDao,
            modifier = modifier.safeDrawingPadding()
        )

        else -> {
            //null hasta que Room responde, asi no aparece la lista vacia por un momento
            val pacientes = remember(perfilActual.id) {
                pacienteDao.getByPerfil(perfilActual.id)
            }
                .collectAsState(initial = null).value
            val paciente = pacientes?.firstOrNull { it.id == pacienteId }
            when {
                pacientes == null -> Box(modifier = modifier.fillMaxSize())
                paciente != null -> PacienteDetalleScreen(
                    paciente = paciente,
                    pacientes = pacientes,
                    medicamentoDao = medicamentoDao,
                    horaMedicaDao = horaMedicaDao,
                    controlTomaDao = controlTomaDao,
                    examenDao = examenDao,
                    alarmaDao = alarmaDao,
                    presionArterialDao = presionArterialDao,
                    onGuardarPaciente = { editado ->
                        scope.launch { withContext(NonCancellable) { pacienteDao.update(editado) } }
                    },
                    onEliminarPaciente = {
                        //primero se sale del paciente, asi no se ve mientras se borra
                        pacienteId = null
                        scope.launch {
                            eliminarPaciente(
                                appContext,
                                paciente,
                                pacienteDao,
                                alarmaDao
                            )
                        }
                    },
                    //cambia solo esa columna, el resto del paciente no se toca
                    onMostrarEmergencia = { mostrar ->
                        scope.launch {
                            withContext(NonCancellable) {
                                pacienteDao.setMostrarEmergencia(paciente.id, mostrar)
                            }
                        }
                    },
                    modifier = modifier,
                    onBack = { pacienteId = null }
                )

                //ya no existe, por ejemplo si se elimino desde su ficha
                pacienteId != null -> LaunchedEffect(pacienteId) {
                    pacienteId = null
                }

                agregando -> AgregarPacienteScreen(
                    perfilId = perfilActual.id,
                    pacientes = pacientes,
                    modifier = modifier.safeDrawingPadding(),
                    onGuardar = { nuevo ->
                        scope.launch { withContext(NonCancellable) { pacienteDao.insert(nuevo) } }
                        agregando = false
                    },
                    onVolver = { agregando = false }
                )

                else -> ListaPacientesScreen(
                    pacientes = pacientes,
                    alarmaDao = alarmaDao,
                    modifier = modifier.safeDrawingPadding(),
                    onPacienteClick = { pacienteId = it.id },
                    onAgregar = { agregando = true }
                )
            }
        }
    }
}

@Composable
private fun ListaPacientesScreen(
    pacientes: List<Paciente>,
    alarmaDao: AlarmaDao,
    modifier: Modifier = Modifier,
    onPacienteClick: (Paciente) -> Unit,
    onAgregar: () -> Unit
) {
    val hayRecordatorios by remember { alarmaDao.hayActivas() }.collectAsState(initial = false)
    //se renueva al volver a la app, asi la edad cambia el dia del cumpleaños
    val hoy = Fechas.millisAIso(rememberAhora())
    var leyendoAviso by rememberSaveable { mutableStateOf(false) }

    if (leyendoAviso) {
        AvisoPrivacidadScreen(onVolver = { leyendoAviso = false }, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Pacientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )
        if (hayRecordatorios) {
            AvisoRecordatorios(modifier = Modifier.padding(bottom = 12.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pacientes.isEmpty()) {
                Text(
                    "Todavía no hay pacientes. Agregue el primero con el botón de abajo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            //el DAO los entrega ordenados por nombre
            pacientes.forEach { paciente ->
                TarjetaPaciente(paciente, hoy, onClick = { onPacienteClick(paciente) })
            }
            //al final de la lista, para leerlo cuando quiera
            TextButton(onClick = { leyendoAviso = true }) { Text("Aviso de privacidad") }
        }
        BotonAgregar(texto = "Agregar paciente", habilitado = true, onClick = onAgregar)
    }
}

@Composable
private fun TarjetaPaciente(paciente: Paciente, hoy: String, onClick: () -> Unit) {
    TarjetaLista(onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarInicial(paciente.nombre)
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    paciente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    paciente.relacion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                paciente.fechaNacimiento?.let {
                    Text(
                        ReglasPaciente.textoEdad(it, hoy),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                //los pacientes antiguos y el Yo del perfil parten sin ficha
                if (!ReglasPaciente.fichaCompleta(paciente)) {
                    //el icono hace notar el aviso de un vistazo, el texto dice que falta
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Faltan datos de la ficha",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

//el formulario va en su propia pantalla, asi la lista no queda tapada por el teclado
@Composable
private fun AgregarPacienteScreen(
    perfilId: Long,
    pacientes: List<Paciente>,
    modifier: Modifier = Modifier,
    onGuardar: (Paciente) -> Unit,
    onVolver: () -> Unit
) {
    BackHandler(onBack = onVolver)
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        BarraVolver(titulo = "Agregar paciente", onVolver = onVolver)
        PacienteFormulario(
            perfilId = perfilId,
            inicial = null,
            pacientes = pacientes,
            onGuardar = onGuardar
        )
    }
}

//Room borra en cascada las filas, pero las alarmas del sistema y los archivos hay que borrarlos aparte
private suspend fun eliminarPaciente(
    context: Context,
    paciente: Paciente,
    pacienteDao: PacienteDao,
    alarmaDao: AlarmaDao
) {
    withContext(NonCancellable) {
        alarmaDao.getIdsByPaciente(paciente.id).forEach { AlarmScheduler.cancelar(context, it) }
        //se leen antes de borrar, despues ya no quedan en la base
        val documentos = pacienteDao.getDocumentos(paciente.id)
        pacienteDao.delete(paciente)
        documentos.forEach { Documentos.borrar(it) }
    }
}