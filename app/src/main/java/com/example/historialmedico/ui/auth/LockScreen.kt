package com.example.historialmedico.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.historialmedico.R
import com.example.historialmedico.ui.historial.rememberAhora
import com.example.historialmedico.util.Fechas

@Composable
fun LockScreen(
    modifier: Modifier = Modifier,
    errorMessage: String?,
    onUnlockClick: () -> Unit,
    showBiometricOption: Boolean,
    onUsePinClick: () -> Unit,
    mostrarEmergencia: Boolean,
    onEmergenciaClick: () -> Unit
) {
    //se renueva al volver a la app, asi el saludo cambia si la app quedo abierta
    val saludo = saludoSegunHora(Fechas.minutosDelDia(rememberAhora()))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EncabezadoAcceso(subtitulo = saludo)
        Text(
            text = if (showBiometricOption) {
                "Sus datos están protegidos. Use su huella o su PIN para entrar."
            } else {
                "Sus datos están protegidos. Ingrese su PIN para entrar."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
        )
        //heightIn y no height en los tres botones: con letra grande crecen y el texto no se corta
        if (showBiometricOption) {
            Button(
                onClick = onUnlockClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_huella),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ingresar con huella")
            }
            Spacer(modifier = Modifier.height(12.dp))
            //con borde y del mismo alto, asi se ve como boton y no como texto suelto
            OutlinedButton(
                onClick = onUsePinClick,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pin),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ingresar con PIN")
            }
        } else {
            //sin huella registrada el PIN es la unica forma de entrar, va como boton principal
            Button(
                onClick = onUsePinClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pin),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ingresar con PIN")
            }
        }
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        if (mostrarEmergencia) {
            Spacer(modifier = Modifier.height(32.dp))
            TarjetaEmergencia(onClick = onEmergenciaClick)
        }
    }
}

//para quien encuentra a la persona, solo aparece si el dueño activo algun paciente
@Composable
private fun TarjetaEmergencia(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "¿Está ayudando en una emergencia?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Vea alergias, medicamentos y contacto sin PIN.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            //56 como los demas botones principales, quien ayuda puede estar apurado
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Información de emergencia", fontWeight = FontWeight.Bold)
            }
        }
    }
}

//de 5:00 a 11:59 dias, de 12:00 a 19:59 tardes, el resto noches
private fun saludoSegunHora(minutosDelDia: Int): String = when (minutosDelDia) {
    in 300..719 -> "Buenos días"
    in 720..1199 -> "Buenas tardes"
    else -> "Buenas noches"
}