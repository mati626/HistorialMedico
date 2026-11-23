package com.example.historialmedico.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class FechasTest {

    @Test
    fun minutosAHora_ochoDeLaManana() {
        assertEquals("08:00", Fechas.minutosAHora(480))
    }

    @Test
    fun minutosAHora_medianoche() {
        assertEquals("00:00", Fechas.minutosAHora(0))
    }

    @Test
    fun minutosAHora_ultimoMinuto() {
        assertEquals("23:59", Fechas.minutosAHora(1439))
    }

    @Test
    fun minutosAHora_rellenoCero() {
        assertEquals("01:01", Fechas.minutosAHora(61))
    }

    @Test
    fun isoAvisible_fechaNormal() {
        assertEquals("18/10/2026", Fechas.isoAVisible("2026-10-18"))
    }

    @Test
    fun isoAvisible_dosPartesIguales() {
        assertEquals("2026-10", Fechas.isoAVisible("2026-10"))
    }

    @Test
    fun isoAvisible_conBarrasQuedaIgual() {
        assertEquals("18/10/2026", Fechas.isoAVisible("18/10/2026"))
    }

    @Test
    fun isoAUtcMillis_primeroDeEnero1970_esCero() {
        assertEquals(0L, Fechas.isoAUtcMillis("1970-01-01"))
    }

    //se fija la zona de Chile para que tambien falle en el CI, que corre en UTC
    //si diaVisible usara la zona del celular, aqui saldria "Viernes 20 de noviembre"
    @Test
    fun diaVisible_noSeCorreAlDiaAnteriorEnChile() {
        val zonaOriginal = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"))
        try {
            assertEquals("Sábado 21 de noviembre", Fechas.diaVisible("2026-11-21"))
        } finally {
            TimeZone.setDefault(zonaOriginal)
        }
    }

    @Test
    fun diaVisible_diaDeUnDigitoSinCero() {
        assertEquals("Jueves 1 de enero", Fechas.diaVisible("2026-01-01"))
    }
}