package com.example.historialmedico

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.lifecycleScope
import com.example.historialmedico.alarms.AlarmScheduler
import com.example.historialmedico.data.AppDataBase
import com.example.historialmedico.export.PdfExporter
import com.example.historialmedico.security.BiometricAuthManager
import com.example.historialmedico.security.PinManager
import com.example.historialmedico.security.ReglasBloqueo
import com.example.historialmedico.security.SalidaInterna
import com.example.historialmedico.ui.auth.CreatePinScreen
import com.example.historialmedico.ui.auth.LockScreen
import com.example.historialmedico.ui.auth.PinEntryScreen
import com.example.historialmedico.ui.historial.PacientesScreen
import com.example.historialmedico.ui.theme.HistorialMedicoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AuthScreen { CREATE_PIN, LOCKED, ENTER_PIN, AUTHENTICATED }
class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var pinManager: PinManager
    private lateinit var database: AppDataBase

    private val permisoNotificacionesLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //Recientes no guarda una imagen con datos de salud; FLAG_SECURE impediria tomar capturas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setRecentsScreenshotEnabled(false)
        biometricAuthManager = BiometricAuthManager(this)
        pinManager = PinManager(this)
        database = AppDataBase.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            AlarmScheduler.reprogramarFaltantes(applicationContext)
            PdfExporter.borrarTemporales(applicationContext)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisoNotificacionesLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            HistorialMedicoTheme {
                var screen by remember { mutableStateOf(if (pinManager.hasPinSet()) AuthScreen.LOCKED else AuthScreen.CREATE_PIN) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var ultimaSalida by remember { mutableStateOf<Long?>(null) }
                var verificando by remember { mutableStateOf(false) }
                var esperaPin by remember { mutableLongStateOf(pinManager.esperaRestante()) }
                //se relanza cada vez que cambia esperaPin: baja el contador hasta llegar a 0
                LaunchedEffect(esperaPin) {
                    if (esperaPin > 0L) {
                        delay(1_000)
                        esperaPin = pinManager.esperaRestante()
                    }
                }
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
                    if (screen == AuthScreen.AUTHENTICATED && salida != null && ReglasBloqueo.debeBloquear(
                            SystemClock.elapsedRealtime() - salida, SalidaInterna.activa
                        )
                    ) {
                        screen = AuthScreen.LOCKED
                    }
                }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    SalidaInterna.limpiar()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screen) {
                        //sin innerPadding: la barra inferior del paciente pone su propio relleno y debe llegar hasta el borde
                        AuthScreen.AUTHENTICATED -> PacientesScreen(
                            perfilDao = database.perfilDao(),
                            pacienteDao = database.pacienteDao(),
                            medicamentoDao = database.medicamentoDao(),
                            horaMedicaDao = database.horaMedicaDao(),
                            examenDao = database.examenDao(),
                            alarmaDao = database.alarmaDao(),
                            presionArterialDao = database.presionArterialDao(),
                            controlTomaDao = database.controlTomaDao(),
                        )

                        AuthScreen.LOCKED -> LockScreen(
                            modifier = Modifier.padding(innerPadding),
                            errorMessage = errorMessage,
                            showBiometricOption = biometricAuthManager.canAuthenticate(),
                            onUnlockClick = {
                                biometricAuthManager.authenticate(
                                    title = "Historial Médico",
                                    subtitle = "Ingrese sus datos para continuar",
                                    negativeButtonText = "Cancelar",
                                    onSuccess = {
                                        screen = AuthScreen.AUTHENTICATED
                                        errorMessage = null
                                        pinManager.reiniciarIntentos()
                                        esperaPin = 0L
                                    },
                                    onError = { _, errString ->
                                        errorMessage = errString.toString()
                                    },
                                    onFailed = {
                                        errorMessage =
                                            "No se reconoce la biometría, intente nuevamente"
                                    }
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
                                lifecycleScope.launch {
                                    withContext(Dispatchers.Default) {
                                        pinManager.setPin(pin)
                                    }
                                    screen = AuthScreen.LOCKED
                                }
                            }
                        )

                        AuthScreen.ENTER_PIN -> PinEntryScreen(
                            modifier = Modifier.padding(innerPadding),
                            errorMessage = errorMessage,
                            esperaMs = esperaPin,
                            verificando = verificando,
                            onPinEntered = { pin ->
                                verificando = true
                                lifecycleScope.launch {
                                    //el hash tarda a proposito: fuera del hilo principal para no congelar la pantalla
                                    val correcto = withContext(Dispatchers.Default) {
                                        pinManager.intentarPin(pin)
                                    }

                                    verificando = false
                                    if (correcto) {
                                        screen = AuthScreen.AUTHENTICATED
                                        errorMessage = null
                                    } else {
                                        esperaPin = pinManager.esperaRestante()
                                        errorMessage = "PIN incorrecto"
                                    }
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
