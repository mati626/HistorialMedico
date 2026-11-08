package com.example.historialmedico

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
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
class PinIncorrectoE2ETest {
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
    fun pruebaPinIncorrecto() {
        ActivityScenario.launch(MainActivity::class.java).use {
            //crea el pin 1234
            composeRule.onNodeWithText("Cree su PIN de acceso").assertIsDisplayed()
            composeRule.onNodeWithText("PIN (4 a 6 dígitos)").performTextInput("1234")
            composeRule.onNodeWithText("Confirme su PIN").performTextInput("1234")
            composeRule.onNodeWithText("Guardar PIN")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("Usar PIN en su lugar").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            //intenta entrar con un pin equivocado
            composeRule.onNodeWithText("Usar PIN en su lugar").performClick()
            composeRule.onNodeWithText("PIN").performTextInput("9999")
            composeRule.onNodeWithText("Desbloquear")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("PIN incorrecto").fetchSemanticsNodes().isNotEmpty()
            }
            //no entro, sigue en la pantalla del pin con el aviso
            composeRule.onNodeWithText("Ingrese su PIN").assertIsDisplayed()
            composeRule.onNodeWithText("PIN incorrecto").assertIsDisplayed()
            //borra el pin equivocado y entra con el correcto
            composeRule.onNodeWithText("PIN").performTextClearance()
            composeRule.onNodeWithText("PIN").performTextInput("1234")
            composeRule.onNodeWithText("Desbloquear")
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithText("Cree su perfil").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Cree su perfil").assertIsDisplayed()
        }
    }
}