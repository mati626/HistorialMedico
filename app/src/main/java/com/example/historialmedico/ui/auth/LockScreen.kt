package com.example.historialmedico.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LockScreen(
    modifier: Modifier= Modifier,
    errorMessage: String?,
    onUnlockClick:()->Unit,
    showBiometricOption: Boolean,
    onUsePinClick:()-> Unit
){
    Column(modifier=modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier= Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier= Modifier.height(24.dp))

        Text(text = "Aplicacion Bloqueada",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Use su huella para acceder a su Historial Medico",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier= Modifier.padding(top=8.dp, bottom = 32.dp)
        )
        if (showBiometricOption){
            Button(
                onClick = onUnlockClick,
                modifier= Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,modifier= Modifier.size(20.dp)
                )
                Spacer(modifier= Modifier.width(8.dp))
                Text("Desbloquear con Biometria")
            }
        }
        TextButton(onClick = onUsePinClick) {Text("Usar PIN en su lugar") }
        errorMessage?.let {
            Spacer(modifier= Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}