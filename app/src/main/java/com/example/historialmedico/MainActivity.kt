package com.example.historialmedico

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.historialmedico.security.BiometricAuthManager
import com.example.historialmedico.ui.theme.HistorialMedicoTheme

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricAuthManager= BiometricAuthManager(this)

        setContent {
            HistorialMedicoTheme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null)}

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   if(isAuthenticated){
                       Greeting(name="Android",modifier=Modifier.padding(innerPadding))
                   }else{
                       LockScreen(
                           modifier= Modifier.padding(innerPadding),
                           errorMessage=errorMessage,
                           onUnlockClick={
                               biometricAuthManager.authenticate(
                                   title="Historial Medico",
                                   subtitle = "Ingrese sus datos para continuar",
                                   negativeButtonText = "Cancelar",
                                   onSuccess = {
                                       isAuthenticated=true
                                       errorMessage=null
                                   },
                                   onError = {_,errString->errorMessage=errString.toString()},
                                   onFailed = {errorMessage="No se reconoce la biometria, intente nuevamente "}
                               )
                           }
                       )
                   }
                }
            }
        }
    }
}

@Composable
fun LockScreen(
    modifier: Modifier=Modifier,
    errorMessage:String?,
    onUnlockClick:()-> Unit
){
    Column(modifier=modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(
            imageVector=Icons.Filled.Lock,
            contentDescription=null,
            modifier= Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier= Modifier.height(24.dp))

        Text(text="Aplicacion Bloqueada",
            style=MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )

        Text(
            text="Use su huella o rostro para acceder a su historial Medico",
            style = MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant,
            modifier= Modifier.padding(top=8.dp,bottom=32.dp)
        )

        Button(
            onClick =onUnlockClick ,
            modifier= Modifier.fillMaxWidth().height(50.dp),colors=ButtonDefaults.buttonColors(
                containerColor= MaterialTheme.colorScheme.primary
            )
        ){
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null, modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Desbloquear con biometria")
    }
        errorMessage?.let{
            Spacer(modifier= Modifier.height(16.dp))
            Text(
                text=it,
                color= MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HistorialMedicoTheme {
        Greeting("Android")
    }
}