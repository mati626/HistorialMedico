package com.example.historialmedico.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasPastillasTest {

    @Test
    fun diasRestantes_diaIncompleto_noSeCuenta() {
        assertEquals(15, ReglasPastillas.diasRestantes(31, 1, 2))
    }

    @Test
    fun diasRestantes_variasPorToma_divideEntreTodas() {
        assertEquals(5, ReglasPastillas.diasRestantes(30, 2, 3))
    }

    @Test
    fun diasRestantes_sinTomas_null() {
        assertNull(ReglasPastillas.diasRestantes(30, 1, 0))
    }

    @Test
    fun quedanPocas_cincoDias_avisa() {
        assertTrue(ReglasPastillas.quedanPocas(5))
    }

    @Test
    fun quedanPocas_seisDias_noAvisa() {
        assertFalse(ReglasPastillas.quedanPocas(6))
    }

    @Test
    fun validarCantidad_cero_error() {
        assertEquals(
            "La cantidad debe estar entre 1 y 999",
            ReglasPastillas.validarCantidad(0)
        )
    }

    @Test
    fun validarCantidad_reponerPasaDelMaximo_error() {
        assertEquals(
            "No puede tener más de 999 pastillas",
            ReglasPastillas.validarCantidad(100, actual = 900)
        )
    }

    @Test
    fun validarCantidad_dentroDelRango_valido() {
        assertNull(ReglasPastillas.validarCantidad(30, actual = 10))
    }

    @Test
    fun validarUnidadesPorToma_sobreElMaximo_error() {
        assertEquals(
            "Las pastillas por toma deben estar entre 1 y 10",
            ReglasPastillas.validarUnidadesPorToma(11)
        )
    }

    @Test
    fun textoRestantes_sinPastillas_seAcabaron() {
        assertEquals("Pastillas: 0 (se acabaron)", ReglasPastillas.textoRestantes(0, 0))
    }

    @Test
    fun textoRestantes_unDia_singular() {
        assertEquals("Pastillas: 2 (alcanzan para 1 día)", ReglasPastillas.textoRestantes(2, 1))
    }
}
