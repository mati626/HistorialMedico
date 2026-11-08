package com.example.historialmedico.security

import org.junit.Assert.assertEquals
import org.junit.Test

class ReglasIntentosTest {
    @Test
    fun espera_cuatroFallos_sinEspera(){
        assertEquals(0L, ReglasIntentos.espera(4))
    }

    @Test
    fun espera_cincoFallos_treintaSegundos(){
        assertEquals(30_000L, ReglasIntentos.espera(5))
    }

    @Test
    fun espera_muchosFallos_quedaEnElTope(){
        assertEquals(1_800_000L, ReglasIntentos.espera(50))
    }

    @Test
    fun restante_descuentaLoTranscurrido(){
        assertEquals(20_000L, ReglasIntentos.restante(5, 10_000L))
    }

    @Test
    fun restante_esperaCumplida_cero(){
        assertEquals(0L, ReglasIntentos.restante(5, 30_000L))
    }

    @Test
    fun texto_redondeaHaciaArriba(){
        assertEquals("Espere 1 segundo", ReglasIntentos.texto(1L))
    }

    @Test
    fun texto_sesentaSegundos_unMinuto(){
        assertEquals("Espere 1 minuto", ReglasIntentos.texto(60_000L))
    }

    @Test
    fun texto_cincoMinutos(){
        assertEquals("Espere 5 minutos", ReglasIntentos.texto(300_000L))
    }
}