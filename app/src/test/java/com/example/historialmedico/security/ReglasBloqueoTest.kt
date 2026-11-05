package com.example.historialmedico.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasBloqueoTest {
    @Test
    fun debeBloquear_salidaNormal_antesDelLimite_noBloquea(){
        assertFalse(ReglasBloqueo.debeBloquear(29_999L, false))
    }

    @Test
    fun debeBloquear_salidaNormal_enElLimite_bloquea(){
        assertTrue(ReglasBloqueo.debeBloquear(30_000L, false))
    }

    @Test
    fun debeBloquear_salidaInterna_a30Segundos_noBloquea(){
        assertFalse(ReglasBloqueo.debeBloquear(30_000L, true))
    }

    @Test
    fun debeBloquear_salidaInterna_enElLimite_bloquea(){
        assertTrue(ReglasBloqueo.debeBloquear(600_000L, true))
    }
}