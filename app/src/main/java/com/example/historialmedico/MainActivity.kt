package com.example.historialmedico

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.lifecycleScope
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.security.BiometricAuthManager
import com.example.historialmedico.security.PinManager
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import com.example.historialmedico.data.AppDataBase
import com.example.historialmedico.security.ReglasBloqueo
import com.example.historialmedico.security.SalidaInterna
import com.example.historialmedico.ui.auth.LockScreen
import com.example.historialmedico.ui.auth.CreatePinScreen
import com.example.historialmedico.ui.auth.PinEntryScreen
import com.example.historialmedico.ui.historial.PacientesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AuthScreen {CREATE_PIN, LOCKED, ENTER_PIN, AUTHENTICATED}
class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var pinManager: PinManager
    private lateinit var database: AppDataBase

    private val permisoNotificacionesLauncher=registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricAuthManager = BiometricAuthManager(this)
        pinManager = PinManager(this)
        database = AppDataBase.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            AlarmScheduler.reprogramarFaltantes(applicationContext)
        }

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED
        ){
            permisoNotificacionesLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            HistorialMedicoTheme {
                var screen by remember { mutableStateOf(if (pinManager.hasPinSet()) AuthScreen.LOCKED else AuthScreen.CREATE_PIN) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var ultimaSalida by remember { mutableStateOf<Long?>(null) }
                BackHandler(enabled = screen == AuthScreen.ENTER_PIN) {
                    screen = AuthScreen.LOCKED
                    errorMessage = null
                }

                //elapsedRealtime sigue contando con la pantalla apagada y no cambia si se ajusta la hora
                LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                    ultimaSalida = SystemClock.elapsedRealtime()
                }

                LifecycleEventEffect(Lifecycle.Event.ON_START) {
                    val salida = ultimaSalida
                    if(screen== AuthScreen.AUTHENTICATED && salida != null && ReglasBloqueo.debeBloquear(
                            SystemClock.elapsedRealtime() - salida, SalidaInterna.activa)){
                        screen = AuthScreen.LOCKED
                    }
                }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    SalidaInterna.limpiar()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screen) {
                        AuthScreen.AUTHENTICATED -> PacientesScreen(
                            perfilDao = database.perfilDao(),
                            pacienteDao = database.pacienteDao(),
                            medicamentoDao = database.medicamentoDao(),
                            horaMedicaDao = database.horaMedicaDao(),
                            examenDao = database.examenDao(),
                            alarmaDao = database.alarmaDao(),
                            presionArterialDao = database.presionArterialDao(),
                            controlTomaDao=database.controlTomaDao(),
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
