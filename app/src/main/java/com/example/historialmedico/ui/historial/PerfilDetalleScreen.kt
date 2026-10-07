package com.example.historialmedico.ui.historial

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.data.ExamenDao
import com.example.historialmedico.data.HoraMedicaDao
import com.example.historialmedico.data.MedicamentoDao
import com.example.historialmedico.data.Perfil
import com.example.historialmedico.export.PdfExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PerfilDetalleScreen(
    perfil: Perfil,
    medicamentoDao: MedicamentoDao,
    horaMedicaDao: HoraMedicaDao,
    examenDao: ExamenDao,
    modifier: Modifier= Modifier,
    onBack:()-> Unit
){
    var tabSeleccionado by remember { mutableStateOf(0) }
    val scope=rememberCoroutineScope()
    val context=LocalContext.current

    BackHandler(onBack=onBack)
    Column(
        modifier=modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp)
    ){
        TextButton(onClick = onBack){
            Text("< Volver a Perfiles")
        }
        Text(
            perfil.nombre,
            style=MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            perfil.relacion,
            style=MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier= Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    val medicamentos = medicamentoDao.getByPerfil(perfil.id).first()
                    val horasMedicas = horaMedicaDao.getByPerfil(perfil.id).first()
                    val examenes = examenDao.getByPerfil(perfil.id).first()
                    val uri = PdfExporter.generarPdf(
                        context,
                        perfil,
                        medicamentos,
                        horasMedicas,
                        examenes
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Exportar Historial"))
                }
            },
            modifier= Modifier.fillMaxWidth()
        ) {
            Text("Exportar PDF")
        }
        Spacer(modifier= Modifier.height(16.dp))

        TabRow(selectedTabIndex = tabSeleccionado) {
            Tab(
                selected = tabSeleccionado==0,
                onClick = {tabSeleccionado=0},
                text = {Text("Medicamentos")}
            )
            Tab(
                selected = tabSeleccionado==1,
                onClick = {tabSeleccionado=1},
                text = {Text("Horas Medicas")}
            )
            Tab(
                selected = tabSeleccionado==2,
                onClick = {tabSeleccionado=2},
                text = {Text("Examenes")}
            )
        }
        Spacer(modifier= Modifier.height(16.dp))
        Column(modifier=Modifier.weight(1f)) {
            when (tabSeleccionado) {
                0 -> MedicamentosSection(dao = medicamentoDao, perfil = perfil)
                1 -> HorasMedicasSection(dao = horaMedicaDao, perfil = perfil)
                2 -> ExamenesSection(dao = examenDao, perfil = perfil)
            }
        }
    }
}