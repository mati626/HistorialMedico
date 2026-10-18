package com.example.historialmedico.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FechasTest {

    @Test
    fun minutosAHora_ochoDeLaManana(){
        assertEquals("08:00", Fechas.minutosAHora(480))
    }

    @Test
    fun minutosAHora_medianoche(){
        assertEquals("00:00", Fechas.minutosAHora(0))
    }

    @Test
    fun minutosAHora_ultimoMinuto(){
        assertEquals("23:59", Fechas.minutosAHora(1439))
    }

    @Test
    fun minutosAHora_rellenoCero(){
        assertEquals("01:01", Fechas.minutosAHora(61))
    }

    @Test
    fun isoAvisible_fechaNormal(){
        assertEquals("18/10/2026", Fechas.isoAVisible("2026-10-18"))
    }

    @Test
    fun isoAvisible_dosPartesIguales(){
        assertEquals("2026-10", Fechas.isoAVisible("2026-10"))
    }

    @Test
    fun isoAvisible_conBarrasQuedaIgual(){
        assertEquals("18/10/2026", Fechas.isoAVisible("18/10/2026"))
    }
}