package com.example.historialmedico

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.historialmedico.data.AppDataBase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PresionE2ETest {
    @get:Rule
    val composeRule = createEmptyComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun dejarAppLimpia() {
        context.deleteSharedPreferences("auth_prefs")
        AppDataBase.getInstance(context).clearAllTables()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(
                    context.packageName,
                    Manifest.permission.POST_NOTIFICATIONS
                )
        }
    }

    @Test
    fun pruebaEntrarRegistrarPresion() {
        ActivityScenario.launch(MainActivity::class.java).use {
            //crea el pin
            composeRule.onNodeWithText("Cree su PIN de Acceso").assertIsDisplayed()
            composeRule.onNodeWithText("PIN (4 a 6 digitos)").performTextInput("1234")
            composeRule.onNodeWithText("Confirme su PIN").performTextInput("1234")
            //accion al boton y no un touch: el teclado mueve la pantalla
            composeRule.onNodeWithText("Guardar PIN")
                .performSemanticsAction(SemanticsActions.OnClick)
            //entrar con el pin
            composeRule.onNodeWithText("Usar PIN en su lugar").performClick()
            composeRule.onNodeWithText("PIN").performTextInput("1234")
            composeRule.onNodeWithText("Desbloquear")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Crea tu perfil").fetchSemanticsNodes().isNotEmpty()
            }
            //crea el perfil y elige al paciente
            composeRule.onNodeWithText("Crea tu perfil").assertIsDisplayed()
            composeRule.onNodeWithText("Tu nombre").performTextInput("Juan Perez")
            composeRule.onNodeWithText("Continuar").performSemanticsAction(SemanticsActions.OnClick)
            //"Yo" es la relacion del titular: solo aparece en la lista de pacientes
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Yo").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Juan Perez").performClick()
            //registrar presion
            composeRule.onNodeWithText("Presion").performScrollTo().performClick()
            composeRule.onNodeWithText("Sistolica").performTextInput("120")
            composeRule.onNodeWithText("Diastolica").performTextInput("80")
            composeRule.onNodeWithText("Agregar Medicion").performScrollTo().performSemanticsAction(
                SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000){
                composeRule.onAllNodesWithText("120/80 mmHg").fetchSemanticsNodes().isNotEmpty()
            }
            //la medicion aparece en la lista como sistolica/diastolica mmHg
            composeRule.onNodeWithText("120/80 mmHg").performScrollTo().assertIsDisplayed()
        }
    }
}