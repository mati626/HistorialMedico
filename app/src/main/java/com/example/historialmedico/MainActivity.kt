package com.example.historialmedico

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.historialmedico.security.BiometricAuthManager
import com.example.historialmedico.security.PinManager
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import com.example.historialmedico.data.AppDataBase
import com.example.historialmedico.ui.auth.LockScreen
import com.example.historialmedico.ui.auth.CreatePinScreen
import com.example.historialmedico.ui.auth.PinEntryScreen
import com.example.historialmedico.ui.historial.PerfilesScreen
enum class AuthScreen {CREATE_PIN, LOCKED, ENTER_PIN, AUTHENTICATED}
//Demo CI/CD - HistorialMedico
class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var pinManager: PinManager
    private lateinit var database: AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricAuthManager = BiometricAuthManager(this)
        pinManager = PinManager(this)
        database = AppDataBase.getInstance(this)

        setContent {
            HistorialMedicoTheme {
                var screen by remember { mutableStateOf(if (pinManager.hasPinSet()) AuthScreen.LOCKED else AuthScreen.CREATE_PIN) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                BackHandler(enabled = screen == AuthScreen.ENTER_PIN) {
                    screen = AuthScreen.LOCKED
                    errorMessage = null
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screen) {
                        AuthScreen.AUTHENTICATED -> PerfilesScreen(
                            dao = database.perfilDao(),
                            medicamentoDao = database.medicamentoDao(),
                            horaMedicaDao = database.horaMedicaDao(),
                            examenDao = database.examenDao(),
                            modifier = Modifier.padding(innerPadding)
                        )
                        AuthScreen.LOCKED -> LockScreen(
                            modifier = Modifier.padding(innerPadding),
                            errorMessage = errorMessage,
                            showBiometricOption = biometricAuthManager.canAuthenticate(),
                            onUnlockClick = {
                                biometricAuthManager.authenticate(
                                    title = "Historial Medico",
                                    subtitle = "Ingrese sus datos para continuar",
                                    negativeButtonText = "Cancelar",
                                    onSuccess = {
                                        screen = AuthScreen.AUTHENTICATED
                                        errorMessage = null
                                    },
                                    onError = { _, errString -> errorMessage = errString.toString() },
                                    onFailed = { errorMessage = "No se reconoce la biometria, intente nuevamente " }
                                )
                            },
                            onUsePinClick = {
                                screen = AuthScreen.ENTER_PIN
                                errorMessage = null
                            }
                        )
                        AuthScreen.CREATE_PIN -> CreatePinScreen(
                            modifier = Modifier.padding(innerPadding),
                            onPinCreated = { pin ->
                                pinManager.setPin(pin)
                                screen = AuthScreen.LOCKED
                            }
                        )
                        AuthScreen.ENTER_PIN -> PinEntryScreen(
                            modifier = Modifier.padding(innerPadding),
                            errorMessage = errorMessage,
                            onPinEntered = { pin ->
                                if (pinManager.validatePin(pin)) {
                                    screen = AuthScreen.AUTHENTICATED
                                    errorMessage = null
                                } else {
                                    errorMessage = "PIN incorrecto"
                                }
                            },
                            onBack = {
                                screen = AuthScreen.LOCKED
                                errorMessage = null
                            }
                        )
                    }
                }
            }
        }
    }
}
