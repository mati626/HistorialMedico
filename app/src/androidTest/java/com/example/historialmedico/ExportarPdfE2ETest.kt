package com.example.historialmedico

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class ExportarPdfE2ETest {
    @get:Rule
    val composeRule = createEmptyComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    
    @Before
    fun dejarAppLimpia(){
        context.deleteSharedPreferences("auth_prefs")
        AppDataBase.getInstance(context).clearAllTables()
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(context.packageName, 
                    Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @Test
    fun pruebaCrearPinEntrarExportar(){
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.onNodeWithText("Cree su PIN de Acceso").assertIsDisplayed()
            composeRule.onNodeWithText("PIN (4 a 6 digitos)").performTextInput("1234")
            composeRule.onNodeWithText("Confirme su PIN").performTextInput("1234")
            composeRule.onNodeWithText("Guardar PIN").performClick()
            composeRule.onNodeWithText("Usar PIN en su lugar").performClick()
            composeRule.onNodeWithText("PIN").performTextInput("1234")
            composeRule.onNodeWithText("Desbloquear").performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Crea tu perfil").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Crea tu perfil").assertIsDisplayed()
            composeRule.onNodeWithText("Tu nombre").performTextInput("Paciente Prueba")
            composeRule.onNodeWithText("Continuar").performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Yo").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Paciente Prueba").performClick()
            composeRule.onNodeWithText("Nombre").performTextInput("Medicamento Prueba")
            composeRule.onNodeWithText("Dosis").performTextInput("10 mg")
            composeRule.onNodeWithText("Agregar Medicamento").performScrollTo().performClick()
            composeRule.waitUntil(timeoutMillis = 5_000){
                composeRule.onAllNodesWithText("Medicamento Prueba - 10 mg").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Medicamento Prueba - 10 mg").performScrollTo().assertIsDisplayed()
        }
    }
}