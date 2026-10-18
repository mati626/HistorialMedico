package com.example.historialmedico.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PinManagerTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var pinManager: PinManager

    @Before
    fun empezarSinPin(){
        context.deleteSharedPreferences("auth_prefs")
        pinManager= PinManager(context)
    }

    @Test
    fun sinPin_noHayPinGuardado(){
        assertFalse(pinManager.hasPinSet())
    }

    @Test
    fun guardarPin_quedaGuardado(){
        pinManager.setPin("1234")
        assertTrue(pinManager.hasPinSet())
    }

    @Test
    fun pinCorrecto_valida(){
        pinManager.setPin("1234")
        assertTrue(pinManager.validatePin("1234"))
    }

    @Test
    fun pinIncorrecto_noValida(){
        pinManager.setPin("1234")
        assertFalse(pinManager.validatePin("4321"))
    }
}