package com.example.historialmedico.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val HOY = "2026-11-14"

class ReglasPacienteTest {

    //paciente con la ficha completa, cada prueba cambia solo lo que revisa
    private fun paciente(
        id: Long = 0,
        nombre: String = "María Pérez",
        relacion: String = "Madre"
    ) = Paciente(
        id = id,
        perfilId = 1,
        nombre = nombre,
        relacion = relacion,
        fechaNacimiento = "1958-05-20",
        sexo = Sexo.FEMENINO,
        estadoCivil = EstadoCivil.CASADO,
        prevision = Prevision.FONASA,
        grupoSanguineo = GrupoSanguineo.NO_SABE
    )

    @Test
    fun edad_cumpleanosYaPaso_cuentaElAnio() {
        assertEquals(68, ReglasPaciente.edad("1958-05-20", HOY))
    }

    @Test
    fun edad_cumpleanosTodaviaNoLlega_unoMenos() {
        assertEquals(67, ReglasPaciente.edad("1958-12-03", HOY))
    }

    @Test
    fun edad_elDiaDelCumpleanos_yaLosCumplio() {
        assertEquals(68, ReglasPaciente.edad("1958-11-14", HOY))
    }

    @Test
    fun edad_nacidoUn29DeFebrero_cumpleEl1DeMarzo() {
        assertEquals(24, ReglasPaciente.edad("2000-02-29", "2025-02-28"))
        assertEquals(25, ReglasPaciente.edad("2000-02-29", "2025-03-01"))
    }

    @Test
    fun validarFechaNacimiento_masDe120Anios_error() {
        assertEquals(
            "Revise la fecha de nacimiento: da más de 120 años",
            ReglasPaciente.validarFechaNacimiento("1905-11-13", HOY)
        )
    }

    @Test
    fun validar_editarElMismoPaciente_noEsRepetido() {
        val guardado = paciente(id = 5)
        assertNull(
            ReglasPaciente.validar(
                guardado.copy(prevision = Prevision.ISAPRE),
                listOf(guardado),
                HOY
            )
        )
    }

    @Test
    fun validar_mismoNombreYRelacion_error() {
        val guardado = paciente(id = 5)
        assertEquals(
            "Ya existe un paciente con ese nombre y esa relación",
            ReglasPaciente.validar(paciente(nombre = "  maría   pérez "), listOf(guardado), HOY)
        )
    }

    @Test
    fun validar_segundoPacienteYo_error() {
        val yo = paciente(id = 1, nombre = "Juan Pérez", relacion = ReglasPaciente.YO)
        assertEquals(
            "Ya hay un paciente registrado como Yo",
            ReglasPaciente.validar(
                paciente(nombre = "Pedro Soto", relacion = ReglasPaciente.YO),
                listOf(yo),
                HOY
            )
        )
    }
}