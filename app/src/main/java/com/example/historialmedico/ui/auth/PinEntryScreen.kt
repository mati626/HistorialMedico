package com.example.historialmedico.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.historialmedico.security.ReglasIntentos
import com.example.historialmedico.security.ReglasPin
import com.example.historialmedico.ui.historial.BarraVolver

@Composable
fun PinEntryScreen(
    modifier: Modifier = Modifier,
    errorMessage: String?,
    esperaMs: Long,
    verificando: Boolean,
    onPinEntered: (String) -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val puedeEntrar = pin.length >= ReglasPin.LARGO_MINIMO && esperaMs == 0L && !verificando
    val mensaje =
        if (esperaMs > 0L) "Demasiados intentos. ${ReglasIntentos.texto(esperaMs)}" else errorMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp)
    ) {
        BarraVolver(titulo = null, onVolver = onBack)
        //ocupa el resto de la pantalla, asi Volver queda arriba y lo demas al centro
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Ingrese su PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
            CampoPin(
                valor = pin,
                onValorChange = { pin = it },
                etiqueta = "PIN",
                error = mensaje,
                enfocarAlAbrir = true,
                //Enter hace lo mismo que el boton, y solo cuando el boton esta habilitado
                onListo = { if (puedeEntrar) onPinEntered(pin) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onPinEntered(pin) },
                enabled = puedeEntrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (verificando) "Verificando..." else "Desbloquear")
            }
        }
    }
}