package com.example.historialmedico

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.historialmedico.data.AppDataBase
import com.example.historialmedico.data.EstadoCivil
import com.example.historialmedico.data.GrupoSanguineo
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.Perfil
import com.example.historialmedico.data.Prevision
import com.example.historialmedico.data.Sexo
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//el flujo completo: activar en la ficha, editar sin que se apague y verlo sin PIN desde el bloqueo
@RunWith(AndroidJUnit4::class)
class EmergenciaE2ETest {
    @get:Rule
    val composeRule = createEmptyComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun dejarAppConDosPacientes() {
        context.deleteSharedPreferences("auth_prefs")
        val db = AppDataBase.getInstance(context)
        db.clearAllTables()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(
                    context.packageName,
                    Manifest.permission.POST_NOTIFICATIONS
                )
        }
        //los pacientes van directo a la base: la prueba es de emergencia, el formulario ya tiene sus reglas probadas
        runBlocking {
            val perfilId = db.perfilDao().insert(Perfil(nombre = "Juan Perez"))
            val juanId = db.pacienteDao().insert(
                Paciente(
                    perfilId = perfilId,
                    nombre = "Juan Perez",
                    relacion = "Yo",
                    fechaNacimiento = "1955-03-14",
                    sexo = Sexo.MASCULINO,
                    estadoCivil = EstadoCivil.CASADO,
                    prevision = Prevision.FONASA,
                    grupoSanguineo = GrupoSanguineo.O_POSITIVO,
                    alergias = "Penicilina",
                    contactoNombre = "Carolina Perez (hija)",
                    contactoTelefono = "+56 9 8765 4321"
                )
            )
            db.medicamentoDao().insert(Medicamento(pacienteId = juanId, nombre = "Enalapril", dosis = "10 mg"))
            //otro paciente sin activar, sus datos no deben verse sin PIN
            db.pacienteDao().insert(Paciente(perfilId = perfilId, nombre = "Rosa Munoz", relacion = "Madre"))
        }
        Intents.init()
    }

    @After
    fun soltarIntents() {
        Intents.release()
    }

    @Test
    fun pruebaActivarEmergenciaYVerlaSinPin() {
        ActivityScenario.launch(MainActivity::class.java).use {
            //crea el pin
            composeRule.onNodeWithText("PIN (4 a 6 dígitos)").performTextInput("1234")
            composeRule.onNodeWithText("Confirme su PIN").performTextInput("1234")
            //accion al boton y no un touch: el teclado mueve la pantalla
            composeRule.onNodeWithText("Guardar PIN")
                .performSemanticsAction(SemanticsActions.OnClick)
            //el hash del pin tarda a proposito, en un emulador lento puede pasar los 5 s
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("Ingresar con PIN").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            //nadie activado todavia, el bloqueo no muestra la tarjeta
            composeRule.onNodeWithText("¿Está ayudando en una emergencia?").assertDoesNotExist()
            //entrar con el pin
            composeRule.onNodeWithText("Ingresar con PIN").performClick()
            composeRule.onNodeWithText("PIN").performTextInput("1234")
            composeRule.onNodeWithText("Desbloquear")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("Juan Perez").fetchSemanticsNodes().isNotEmpty()
            }
            //activar desde la ficha, con la confirmacion
            composeRule.onNodeWithText("Juan Perez").performClick()
            composeRule.onNodeWithText("Ficha").performClick()
            composeRule.onNodeWithText("Mostrar en la pantalla de bloqueo").performScrollTo()
                .performClick()
            composeRule.onNodeWithText("Sí, mostrar").performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodes(hasText("Mostrar en la pantalla de bloqueo") and isOn())
                    .fetchSemanticsNodes().isNotEmpty()
            }
            //editar al paciente no debe apagar la emergencia
            composeRule.onNodeWithText("Editar datos").performScrollTo().performClick()
            composeRule.onNodeWithText("Alergias").performScrollTo()
                .performTextReplacement("Penicilina y sulfas")
            composeRule.onNodeWithText("Guardar paciente").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick)
            //cuando la ficha muestra la alergia nueva, el cambio ya se guardo en la base
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Penicilina y sulfas").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNode(hasText("Mostrar en la pantalla de bloqueo") and isOn())
                .performScrollTo().assertIsDisplayed()
        }
        //al abrir la app de nuevo queda bloqueada
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Información de emergencia").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithText("Información de emergencia").performScrollTo().performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Penicilina y sulfas").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithText("Enalapril").performScrollTo().assertIsDisplayed()
            //Rosa no se activo, no aparece
            composeRule.onNodeWithText("Rosa Munoz").assertDoesNotExist()
            //llamar sin abrir el marcador de verdad, se revisa el numero que recibiria
            intending(hasAction(Intent.ACTION_DIAL))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
            composeRule.onNodeWithText("Llamar a Carolina").performScrollTo().performClick()
            intended(allOf(hasAction(Intent.ACTION_DIAL), hasData(Uri.parse("tel:+56987654321"))))
        }
    }
}