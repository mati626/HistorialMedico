package com.example.historialmedico.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasEmergenciaTest {

    @Test
    fun paraMarcar_conMasYEspacios_dejaElMasYLosNumeros() {
        assertEquals("+56987654321", ReglasEmergencia.paraMarcar("+56 9 8765 4321"))
    }

    @Test
    fun paraMarcar_conGuionSinMas_soloNumeros() {
        assertEquals("987654321", ReglasEmergencia.paraMarcar("9 8765-4321"))
    }

    @Test
    fun validarTelefono_celularConCodigo_valido() {
        assertNull(ReglasEmergencia.validarTelefono("+56 9 8765 4321"))
    }

    @Test
    fun validarTelefono_sieteDigitos_errorDeLargo() {
        assertEquals(
            "El teléfono debe tener entre 8 y 15 dígitos",
            ReglasEmergencia.validarTelefono("876 5432")
        )
    }

    @Test
    fun validarContacto_soloNombre_pideElTelefono() {
        assertEquals(
            "Falta el teléfono del contacto de emergencia",
            ReglasEmergencia.validarContacto("Carolina Muñoz", "")
        )
    }

    @Test
    fun validarContacto_soloTelefono_pideElNombre() {
        //solo espacios cuenta como vacio
        assertEquals(
            "Falta el nombre del contacto de emergencia",
            ReglasEmergencia.validarContacto("  ", "+56 9 8765 4321")
        )
    }

    @Test
    fun validarContacto_nombreConParentesco_valido() {
        //el parentesco entre parentesis ayuda a quien llama
        assertNull(ReglasEmergencia.validarContacto("Carolina Muñoz (hija)", "+56 9 8765 4321"))
    }
}