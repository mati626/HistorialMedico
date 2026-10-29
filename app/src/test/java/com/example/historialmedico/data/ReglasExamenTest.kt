package com.example.historialmedico.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val HOY = "2026-10-24"

class ReglasExamenTest {

    @Test
    fun estado_proximoSinFecha_solicitado() {
        assertEquals(
            EstadoExamen.SOLICITADO,
            ReglasExamen.estado(false, null)
        )
    }

    @Test
    fun estado_proximoConFecha_agendado() {
        assertEquals(EstadoExamen.AGENDADO, ReglasExamen.estado(false, "2026-11-05"))
    }

    @Test
    fun estado_realizadoConFecha_realizado() {
        assertEquals(EstadoExamen.REALIZADO, ReglasExamen.estado(true, "2026-10-20"))
    }

    @Test
    fun validarFecha_realizadoSinFecha_errorObligatorio() {
        assertEquals(
            "Indique la fecha en que se realizó el examen",
            ReglasExamen.validarFecha(true, null, HOY)
        )
    }

    @Test
    fun validarFecha_proximoSinFecha_valido() {
        assertNull(ReglasExamen.validarFecha(false, null, HOY))
    }

    @Test
    fun validarFecha_realizadoConFechaFutura_error() {
        assertEquals(
            "Un examen realizado no puede tener fecha futura",
            ReglasExamen.validarFecha(true, "2026-11-05", HOY)
        )
    }

    @Test
    fun validarFecha_proximoConFechaPasada_error() {
        assertEquals(
            "Un examen próximo no puede tener fecha pasada",
            ReglasExamen.validarFecha(false, "2026-10-20", HOY)
        )
    }

    @Test
    fun validarFecha_realizadoHoy_valido() {
        assertNull(ReglasExamen.validarFecha(true, HOY, HOY))
    }

    @Test
    fun validarFecha_proximoHoy_valido() {
        assertNull(ReglasExamen.validarFecha(false, HOY, HOY))
    }
}