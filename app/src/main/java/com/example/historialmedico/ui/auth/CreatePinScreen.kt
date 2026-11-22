package com.example.historialmedico.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.historialmedico.security.ReglasPin
import kotlinx.coroutines.flow.first

@Composable
fun CreatePinScreen(
    modifier: Modifier = Modifier,
    onPinCreated: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorPin by remember { mutableStateOf<String?>(null) }
    var errorConfirmacion by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    val focoPin = remember { FocusRequester() }
    val ventana = LocalWindowInfo.current

    //el teclado solo se abre si la ventana de la app esta activa, al abrir por primera vez
    //el aviso de notificaciones queda encima, por eso se espera a que la ventana este activa
    LaunchedEffect(Unit) {
        snapshotFlow { ventana.isWindowFocused }.first { it }
        focoPin.requestFocus()
    }

    //el hash del PIN tarda a proposito, guardando evita que un segundo toque lo guarde otra vez
    fun guardar() {
        if (guardando) return
        errorPin = ReglasPin.errorDeLargo(pin)
        errorConfirmacion = ReglasPin.errorDeConfirmacion(pin, confirmPin)
        if (errorPin == null && errorConfirmacion == null) {
            guardando = true
            onPinCreated(pin)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EncabezadoAcceso(subtitulo = "Cree su PIN de acceso")
        Text(
            text = "Se pedirá cada vez que abra la aplicación. Si su teléfono tiene huella, también podrá usarla.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
        )
        CampoPin(
            valor = pin,
            onValorChange = {
                pin = it
                //si cambia el PIN los dos mensajes pueden dejar de ser ciertos
                errorPin = null
                errorConfirmacion = null
            },
            etiqueta = "PIN (4 a 6 dígitos)",
            error = errorPin,
            modifier = Modifier.focusRequester(focoPin)
        )
        Spacer(modifier = Modifier.height(12.dp))
        CampoPin(
            valor = confirmPin,
            onValorChange = {
                confirmPin = it
                errorConfirmacion = null
            },
            etiqueta = "Confirme su PIN",
            error = errorConfirmacion,
            onListo = { guardar() }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { guardar() },
            enabled = !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(if (guardando) "Guardando..." else "Guardar PIN")
        }
    }
}