package com.example.historialmedico.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.historialmedico.R
import com.example.historialmedico.security.ReglasPin
import kotlinx.coroutines.flow.first

//logo y nombre de la app, lo comparten Bloqueo y Crear PIN, solo cambia la linea de abajo
@Composable
fun EncabezadoAcceso(subtitulo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(136.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Historial Médico",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

//campo de PIN que usan Crear PIN e ingresar PIN, el error va bajo el campo que esta mal
//con onListo la tecla del teclado confirma, sin onListo pasa al campo siguiente
@Composable
fun CampoPin(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    enfocarAlAbrir: Boolean = false,
    onListo: (() -> Unit)? = null
) {
    val foco = remember { FocusRequester() }
    val ventana = LocalWindowInfo.current

    //si hay un aviso encima Android ignora el pedido de teclado, por eso se espera a que la ventana de la app este activa
    if (enfocarAlAbrir) {
        LaunchedEffect(Unit) {
            snapshotFlow { ventana.isWindowFocused }.first { it }
            foco.requestFocus()
        }
    }

    OutlinedTextField(
        value = valor,
        onValueChange = {
            if (it.length <= ReglasPin.LARGO_MAXIMO && it.all(Char::isDigit)) onValorChange(it)
        },
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = if (error != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(error)
                }
            }
        } else {
            null
        },
        singleLine = true,
        //sin alto fijo: si la persona aumenta el tamaño de la letra del celular el campo crece y no se corta
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            textAlign = TextAlign.Center,
            letterSpacing = 8.sp
        ),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = if (onListo != null) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onDone = { onListo?.invoke() }),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(foco)
    )
}