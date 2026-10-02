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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CreatePinScreen(
    modifier: Modifier= Modifier,
    onPinCreated:(String)-> Unit
){
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier=modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier= Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier= Modifier.height(16.dp))

        Text("Cree su PIN de Acceso", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text("Su pin se usara si la biometria falla",
            style = MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant,
            modifier= Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        OutlinedTextField(
            value = pin,
            onValueChange = {if(it.length<=6&&it.all(Char::isDigit))pin=it},
            label = {Text("PIN (4 a 6 digitos")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier= Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = {if(it.length<=6&&it.all (Char::isDigit))confirmPin=it},
            label = {Text("Confirme su PIN")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(modifier= Modifier.height(12.dp))
            Text(it,color=MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier= Modifier.height(24.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            onClick = {
                when{
                    pin.length<4->error="El PIN debe tener minimo 4 digitos"
                    pin != confirmPin->error="Los PIN no coinciden"
                    else->onPinCreated(pin)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Guardar PIN")
        }
    }
}