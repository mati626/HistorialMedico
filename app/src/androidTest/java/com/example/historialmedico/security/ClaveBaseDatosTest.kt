package com.example.historialmedico.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

//no borra bd_prefs: las pruebas E2E abren la base con esta misma clave
@RunWith(AndroidJUnit4::class)
class ClaveBaseDatosTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun obtener_dosVeces_devuelveLaMismaClave() {
        val primera = ClaveBaseDatos.obtener(context)
        val segunda = ClaveBaseDatos.obtener(context)
        assertArrayEquals(primera, segunda)
    }

    @Test
    fun obtener_claveDe64CaracteresHexadecimales() {
        val clave = String(ClaveBaseDatos.obtener(context))
        assertEquals(64, clave.length)
        assertTrue(clave.all { it in '0'..'9' || it in 'a'..'f' })
    }
}