package com.example.historialmedico.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasPinTest {

    @Test
    fun errorDeLargo_tresDigitos_errorDeMinimo(){
        assertEquals("El PIN debe tener mínimo 4 dígitos", ReglasPin.errorDeLargo("123"))
    }

    @Test
    fun errorDeLargo_minimoExacto(){
        assertNull(ReglasPin.errorDeLargo("1234"))
    }

    @Test
    fun errorDeLargo_campoMaximo(){
        assertNull(ReglasPin.errorDeLargo("123456"))
    }

    @Test
    fun errorDeConfirmacion_distintos_errorDeCoincidencia(){
        assertEquals("Los PIN no coinciden", ReglasPin.errorDeConfirmacion("1234","1235"))
    }

    @Test
    fun errorDeConfirmacion_iguales(){
        assertNull(ReglasPin.errorDeConfirmacion("1234","1234"))
    }
}