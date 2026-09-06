package com.example.historialmedico

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    Column(modifier=modifier) {
        Text("Aplicacion Bloqueada")
        Button(onClick = onUnlockClick){
            Text("Desbloquear con Biometria")
        }
        errorMessage?.let { Text(it) }
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