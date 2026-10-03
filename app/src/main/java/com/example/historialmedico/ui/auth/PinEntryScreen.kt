package com.example.historialmedico.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PinEntryScreen(
    modifier: Modifier= Modifier,
    errorMessage: String?,
    onPinEntered: (String)->Unit,
    onBack:()-> Unit
){
    var pin by remember { mutableStateOf("") }

    Column(
        modifier=modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier=Modifier.size(64.dp),
            tint=MaterialTheme.colorScheme.primary
        )
        Spacer(modifier= Modifier.height(16.dp))
        Text("Ingrese su Pin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier= Modifier.height(24.dp))

        OutlinedTextField(
            value=pin,
            onValueChange={if (it.length <=6 && it.all(Char::isDigit))pin=it},
            label={Text("PIN")},
            visualTransformation= PasswordVisualTransformation(),
            keyboardOptions= KeyboardOptions(keyboardType =
                KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        errorMessage?.let{
            Spacer(modifier= Modifier.height(12.dp))
            Text(it,color=MaterialTheme.colorScheme.error,style= MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier= Modifier.height(24.dp))
        Button(
            colors= ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            onClick = {onPinEntered(pin)},
            modifier= Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Desbloquear")
        }
        TextButton(onClick = onBack,modifier= Modifier.padding(top=8.dp)) {
            Text("Volver")
        }
    }
}