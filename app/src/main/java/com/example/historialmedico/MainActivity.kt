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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.paint
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.tooling.preview.Preview
import com.example.historialmedico.security.BiometricAuthManager
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import com.example.historialmedico.security.PinManager

enum class AuthScreen {CREATE_PIN, LOCKED, ENTER_PIN, AUTHENTICATED}
class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var pinManager: PinManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricAuthManager= BiometricAuthManager(this)
        pinManager= PinManager(this)

        setContent {
            HistorialMedicoTheme {
                var screen by remember { mutableStateOf(if (pinManager.hasPinSet()) AuthScreen.LOCKED else AuthScreen.CREATE_PIN)
                }
                var errorMessage by remember { mutableStateOf<String?>(null)}

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   when(screen){
                       AuthScreen.AUTHENTICATED->Greeting(name="Android",modifier=Modifier.padding(innerPadding))
                       AuthScreen.LOCKED-> LockScreen(
                           modifier= Modifier.padding(innerPadding),
                           errorMessage=errorMessage,
                           showBiometricOption=biometricAuthManager.canAuthenticate(),
                           onUnlockClick={
                               biometricAuthManager.authenticate(
                                   title="Historial Medico",
                                   subtitle = "Ingrese sus datos para continuar",
                                   negativeButtonText = "Cancelar",
                                   onSuccess = {
                                       screen= AuthScreen.AUTHENTICATED
                                       errorMessage=null
                                   },
                                   onError = {_,errString->errorMessage=errString.toString()},
                                   onFailed = {errorMessage="No se reconoce la biometria, intente nuevamente "}
                               )
                           },
                           onUsePinClick={
                               screen= AuthScreen.ENTER_PIN
                               errorMessage=null
                           }
                       )

                       AuthScreen.CREATE_PIN->CreatePinScreen(
                           modifier = Modifier.padding(innerPadding),
                           onPinCreated = {
                               pin->pinManager.setPin(pin)
                               screen= AuthScreen.LOCKED
                           }
                       )
                       AuthScreen.ENTER_PIN->PinEntryScreen(
                           modifier = Modifier.padding(innerPadding),
                           errorMessage=errorMessage,
                           onPinEntered = {
                               pin->
                               if(pinManager.validatePin(pin)){
                                   screen= AuthScreen.AUTHENTICATED
                                   errorMessage=null
                           }else{
                               errorMessage="PIN incorrecto"
                               }
                           },
                           onBack = {
                               screen= AuthScreen.LOCKED
                               errorMessage=null
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
    onUnlockClick:()-> Unit,
    showBiometricOption: Boolean,
    onUsePinClick:()-> Unit
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

            if(showBiometricOption) {
                Button(
                    onClick = onUnlockClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null, modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desbloquear con biometria")
                }
            }
                TextButton(onClick = onUsePinClick) {Text("Usar Pin en su lugar") }
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
fun CreatePinScreen(
    modifier: Modifier= Modifier,
    onPinCreated:(String)->Unit
){
    var pin by remember { mutableStateOf("")}
    var confirmPin by remember {mutableStateOf("")}
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier=modifier.fillMaxSize().padding(24.dp),
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
        Text(
            "Su pin se usara si la biometria falla",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier= Modifier.padding(top=8.dp,bottom=24.dp)
        )
        OutlinedTextField(
            value = pin,
            onValueChange = {if (it.length<=6 && it.all(Char::isDigit))pin = it},
            label = {Text("PIN (4 a 6 digitos)")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier= Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { if (it.length <=6 && it.all(Char::isDigit)) confirmPin= it},
            label = {Text("Confirme su PIN")},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType= KeyboardType.NumberPassword),
            modifier= Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(modifier= Modifier.height(12.dp))
            Text(it,color= MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier= Modifier.height(24.dp))

        Button(
            colors= ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    onClick = {
                when {
                    pin.length < 4 -> error = "El PIN debe tener minimo 4 digitos"
                    pin != confirmPin->error= "Los PIN no coinciden"
                    else -> onPinCreated(pin)
                }
            },
            modifier= Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Guardar PIN")
        }
    }
}
@Composable
fun PinEntryScreen(
    modifier: Modifier= Modifier,
    errorMessage: String?,
    onPinEntered: (String)->Unit,
    onBack:()-> Unit
){
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
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