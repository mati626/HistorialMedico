package com.example.historialmedico

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
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
class PastillasE2ETest {
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
    fun pruebaMarcarTomaDescuentaPastillas() {
        ActivityScenario.launch(MainActivity::class.java).use {
            //crea el pin
            composeRule.onNodeWithText("Cree su PIN de acceso").assertIsDisplayed()
            composeRule.onNodeWithText("PIN (4 a 6 dígitos)").performTextInput("1234")
            composeRule.onNodeWithText("Confirme su PIN").performTextInput("1234")
            composeRule.onNodeWithText("Guardar PIN")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("Usar PIN en su lugar").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            //entrar con el pin
            composeRule.onNodeWithText("Usar PIN en su lugar").performClick()
            composeRule.onNodeWithText("PIN").performTextInput("1234")
            composeRule.onNodeWithText("Desbloquear")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("Cree su perfil").fetchSemanticsNodes().isNotEmpty()
            }
            //crea el perfil y elige al paciente
            composeRule.onNodeWithText("Su nombre").performTextInput("Juan Perez")
            composeRule.onNodeWithText("Continuar").performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Yo").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Juan Perez").performClick()
            //crea un medicamento con un recordatorio y 10 pastillas
            composeRule.onNodeWithText("Historial").performClick()
            composeRule.onNodeWithText("Medicamentos").performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodes(hasText("Agregar medicamento") and isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Agregar medicamento").performClick()
            composeRule.onNodeWithText("Nombre").performTextInput("Enalapril Maleato")
            composeRule.onNodeWithText("Dosis").performTextInput("10 mg")
            //el reloj propone la hora actual, sirve porque Hoy muestra todas las tomas del dia
            composeRule.onNodeWithText("Agregar hora").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.onNodeWithText("Aceptar").performClick()
            composeRule.onNodeWithText("Pastillas que tiene (opcional)").performScrollTo()
                .performTextInput("10")
            composeRule.onNodeWithText("Guardar medicamento").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Pastillas: 10 (alcanzan para 10 días)")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            //marca la toma desde Hoy
            composeRule.onNodeWithText("Hoy").performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Marcar como tomada").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithText("Marcar como tomada")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Tomada").fetchSemanticsNodes().isNotEmpty()
            }
            //Historial vuelve a la lista de medicamentos, que quedo abierta
            composeRule.onNodeWithText("Historial").performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Pastillas: 9 (alcanzan para 9 días)")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Pastillas: 9 (alcanzan para 9 días)").assertIsDisplayed()
        }
    }
}