package com.example.historialmedico.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasTextoTest {

    @Test
    fun normalizar_espaciosRepetidos() {
        assertEquals("Juan Pérez", ReglasTexto.normalizar(" Juan   Pérez  "))
    }

    @Test
    fun validar_soloEspacios_errorObligatorio() {
        assertEquals(
            "El campo Nombre es obligatorio",
            ReglasTexto.validar("   ", ReglasTexto.CampoTexto.NOMBRE_PERSONA)
        )
    }

    @Test
    fun normalizar_saltoDeLinea() {
        //un Enter dentro del campo queda como un espacio
        assertEquals("Juan Pérez", ReglasTexto.normalizar("Juan\nPérez"))
    }

    @Test
    fun validar_unoSobreElMaximo_errorDeLargo() {
        //51 caracteres: uno mas que el maximo
        assertEquals(
            "El campo Nombre admite máximo 50 caracteres",
            ReglasTexto.validar(
                "a".repeat(51),
                ReglasTexto.CampoTexto.NOMBRE_PERSONA
            )
        )
    }

    @Test
    fun validar_numeroEnNombre_errorDeCaracteres() {
        assertEquals(
            "El campo Nombre solo admite letras, espacios, ' y -",
            ReglasTexto.validar("Juan3", ReglasTexto.CampoTexto.NOMBRE_PERSONA)
        )
    }

    @Test
    fun validar_emojiEnMedicamento_errorDeCaracteres() {
        assertEquals(
            "El campo Nombre del medicamento solo admite ${ReglasTexto.TipoTexto.GENERAL.permitidos}",
            ReglasTexto.validar("Paracetamol 😑", ReglasTexto.CampoTexto.MEDICAMENTO)
        )
    }

    @Test
    fun validar_simbolosEnTipoExamen_errorDeCaracteres() {
        assertEquals(
            "El campo Tipo de examen solo admite ${ReglasTexto.TipoTexto.GENERAL.permitidos}",
            ReglasTexto.validar("<script>", ReglasTexto.CampoTexto.TIPO_EXAMEN)
        )
    }

    @Test
    fun validar_maximoExacto_valido() {
        assertNull(ReglasTexto.validar("a".repeat(50), ReglasTexto.CampoTexto.NOMBRE_PERSONA))
    }

    @Test
    fun validar_tildesYEnie_valido() {
        assertNull(ReglasTexto.validar("José Muñoz", ReglasTexto.CampoTexto.NOMBRE_PERSONA))
    }

    @Test
    fun validar_resultadoVacioOpcional_valido() {
        assertNull(ReglasTexto.validar("", ReglasTexto.CampoTexto.RESULTADO, obligatorio = false))
    }

    @Test
    fun validar_espaciosNoCuentanEnElLargo_valido() {
        assertNull(
            ReglasTexto.validar(
                "  " + "a".repeat(50) + "  ",
                ReglasTexto.CampoTexto.NOMBRE_PERSONA
            )
        )
    }
}