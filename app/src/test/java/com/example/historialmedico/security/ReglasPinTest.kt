package com.example.historialmedico.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasPinTest {

    @Test
    fun validarNuevoPin_tresDigitos_errorDeMinimo(){
        assertEquals("El PIN debe tener minimo 4 digitos", ReglasPin.validarNuevoPin("123","123"))
    }

    @Test
    fun validarNuevoPin_errorDeCoincidencia(){
        assertEquals("Los PIN no coinciden", ReglasPin.validarNuevoPin("1234","1235"))
    }

    @Test
    fun validarNuevoPin_minimoExacto(){
        assertNull(ReglasPin.validarNuevoPin("1234","1234"))
    }

    @Test
    fun validarNuevoPin_campoMaximo(){
        assertNull(ReglasPin.validarNuevoPin("123456","123456"))
    }
}