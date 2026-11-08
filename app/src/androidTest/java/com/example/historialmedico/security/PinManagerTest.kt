package com.example.historialmedico.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


@RunWith(AndroidJUnit4::class)
class PinManagerTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var pinManager: PinManager

    @Before
    fun empezarSinPin() {
        context.deleteSharedPreferences("auth_prefs")
        pinManager = PinManager(context)
    }

    @Test
    fun sinPin_noHayPinGuardado() {
        assertFalse(pinManager.hasPinSet())
    }

    @Test
    fun guardarPin_quedaGuardado() {
        pinManager.setPin("1234")
        assertTrue(pinManager.hasPinSet())
    }

    @Test
    fun pinCorrecto_valida() {
        pinManager.setPin("1234")
        assertTrue(pinManager.intentarPin("1234"))
    }

    @Test
    fun pinIncorrecto_noValida() {
        pinManager.setPin("1234")
        assertFalse(pinManager.intentarPin("4321"))
    }

    @Test
    fun cincoFallos_rechazaElPinCorrecto() {
        pinManager.setPin("1234")
        repeat(ReglasIntentos.INTENTOS_LIBRES) { pinManager.intentarPin("0000") }
        assertTrue(pinManager.esperaRestante() > 0L)
        assertFalse(pinManager.intentarPin("1234"))
    }

    @Test
    fun pinAntiguo_entraYQuedaEnFormatoNuevo() {
        val prefs = EncryptedSharedPreferences.create(
            context, "auth_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.edit().putString(
            "pin_hash",
            "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
        ).commit()

        assertTrue(pinManager.intentarPin("1234"))

        assertFalse(HashPin.esAntiguo(prefs.getString("pin_hash", null)!!))
        assertTrue(pinManager.intentarPin("1234"))
    }
}