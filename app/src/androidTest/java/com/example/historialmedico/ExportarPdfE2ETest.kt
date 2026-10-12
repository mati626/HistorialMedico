package com.example.historialmedico

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.historialmedico.data.AppDataBase
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class ExportarPdfE2ETest {
    @get:Rule
    val composeRule = createEmptyComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    //dejar la app como recien instalada sin datos
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
        File(context.cacheDir, "pdfs").deleteRecursively()
        Intents.init()
    }

    @After
    fun soltarIntents() {
        Intents.release()
    }

    @Test
    fun pruebaCrearPinEntrarExportar() {
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
            composeRule.onNodeWithText("Tu nombre").performTextInput("Paciente Prueba")
            composeRule.onNodeWithText("Continuar").performSemanticsAction(SemanticsActions.OnClick)
            //"Yo" es la relacion del titular: solo aparece en la lista de pendientes
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Yo").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Paciente Prueba").performClick()
            composeRule.onNodeWithText("Nombre").performTextInput("Medicamento Prueba")
            composeRule.onNodeWithText("Dosis").performTextInput("10 mg")
            composeRule.onNodeWithText("Agregar Medicamento").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("Medicamento Prueba - 10 mg").fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithText("Medicamento Prueba - 10 mg").performScrollTo()
                .assertIsDisplayed()
            //exportar pdf sin abrir las opciones de sistema
            intending(hasAction(Intent.ACTION_CHOOSER))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
            composeRule.onNodeWithText("Exportar PDF").performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                Intents.getIntents().any { it.action == Intent.ACTION_CHOOSER }
            }
            intended(
                allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(
                        equalTo(Intent.EXTRA_INTENT),
                        allOf(hasAction(Intent.ACTION_SEND), hasType("application/pdf"))
                    )
                )
            )
            val archivos= File(context.cacheDir, "pdfs").listFiles().orEmpty()
            assertEquals(1, archivos.size)
            assertTrue(archivos[0].length()>0)
        }
    }
}
