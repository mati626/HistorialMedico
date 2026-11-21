package com.example.historialmedico.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.historialmedico.ui.historial.BarraVolver
import com.example.historialmedico.ui.historial.BloqueDetalle

//cada punto es un titulo y su texto, en el orden en que se muestran
private val puntosPrivacidad = listOf(
    "Dónde se guardan" to "Solo en este teléfono, dentro de la aplicación. No hay cuentas ni servidores: nadie más recibe sus datos, tampoco quien hizo la aplicación.",
    "Copias de seguridad" to "Sus datos no se copian a la nube ni a otro telefono. Si pierde o cambia de telefono no se pueden recuperar",
    "Acceso" to "Para entrar se pide su PIN o su huella. El PIN se guarda cifrado.",
    "Recordatorios" to "Las notificaciones muestran el nombre del paciente y el medicamento o la hora médica. En la pantalla bloqueada solo dicen \"Tiene un recordatorio\", salvo que su teléfono esté configurado para mostrar todo.",
    "Escáner de documentos" to "El escáner y la lectura del texto usan herramientas de Google que funcionan dentro del teléfono. Google recibe datos técnicos de uso, como tiempos de respuesta, pero no las fotos ni el texto leído.",
    "Compartir" to "Sus datos solo salen del teléfono si usted comparte el historial en PDF, por la aplicación que elija. Quien lo reciba podrá verlo. La copia temporal del PDF se borra la próxima vez que se inicia la aplicación.",
    "Otras personas" to "Si registra a un familiar u otra persona, hágalo con su autorización o la de quien la represente.",
    "Sus derechos" to "Puede ver, corregir y eliminar cada registro. Al eliminar un paciente se borra todo lo suyo. Al desinstalar la aplicación se borran todos los datos."
)

//aviso de privacidad, se abre al crear el perfil y desde la lista de pacientes
@Composable
fun AvisoPrivacidadScreen(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onVolver)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        BarraVolver(titulo = "Aviso de privacidad", onVolver = onVolver)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Su información de salud es un dato sensible según la Ley 21.719 de protección de datos personales. Así la cuida esta aplicación:",
                style = MaterialTheme.typography.bodyLarge
            )
            puntosPrivacidad.forEach { (titulo, texto) ->
                BloqueDetalle(titulo) {
                    Text(texto, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}