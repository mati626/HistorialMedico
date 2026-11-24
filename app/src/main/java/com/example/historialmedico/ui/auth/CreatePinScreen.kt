package com.example.historialmedico.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.historialmedico.security.ReglasPin

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
            enfocarAlAbrir = true
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
            //heightIn y no height: con letra grande el boton crece y el texto no se corta
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(if (guardando) "Guardando..." else "Guardar PIN")
        }
    }
}