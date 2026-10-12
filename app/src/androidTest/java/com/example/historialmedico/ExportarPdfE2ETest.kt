package com.example.historialmedico

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        }
    }
}