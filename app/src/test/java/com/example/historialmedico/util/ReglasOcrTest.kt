package com.example.historialmedico.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ReglasOcrTest {

    @Test
    fun palabrasANumeros_dosisEnPalabras() {
        //el nombre conserva su tilde, solo cambia el numero
        assertEquals("Losartán 50 mg", ReglasOcr.palabrasANumeros("Losartán cincuenta mg"))
    }

    @Test
    fun palabrasANumeros_compuestoConY() {
        assertEquals("132", ReglasOcr.palabrasANumeros("ciento treinta y dos"))
    }

    @Test
    fun palabrasANumeros_miles() {
        assertEquals("2500 mg", ReglasOcr.palabrasANumeros("dos mil quinientos mg"))
    }

    @Test
    fun palabrasANumeros_sinTildeYMayusculas() {
        //el OCR puede perder la tilde o leer todo en mayusculas
        assertEquals("16", ReglasOcr.palabrasANumeros("dieciseis"))
        assertEquals("16", ReglasOcr.palabrasANumeros("DIECISÉIS"))
        assertEquals("22", ReglasOcr.palabrasANumeros("veintidós"))
    }

    @Test
    fun palabrasANumeros_parteDeOtraPalabra_noCambia() {
        //"dos" dentro de "Dosis" y "una" dentro de "Unasyn" no son numeros
        assertEquals("Dosis", ReglasOcr.palabrasANumeros("Dosis"))
        assertEquals("Unasyn", ReglasOcr.palabrasANumeros("Unasyn"))
        assertEquals("", ReglasOcr.palabrasANumeros(""))
    }

    @Test
    fun palabrasANumeros_dosNumerosSeguidos() {
        //asi se lee una presion escrita: sistolica y diastolica juntas
        assertEquals("120 80", ReglasOcr.palabrasANumeros("ciento veinte ochenta"))
    }

    @Test
    fun palabrasANumeros_ordenInvalido_separa() {
        //"tres" no cabe despues de "dos": son dos numeros, no 5
        assertEquals("2 3", ReglasOcr.palabrasANumeros("dos tres"))
    }

    @Test
    fun palabrasANumeros_yQueNoEsNumero_seMantiene() {
        //la "y" solo se une si despues viene otro numero
        assertEquals("5 y media", ReglasOcr.palabrasANumeros("cinco y media"))
    }

    @Test
    fun palabrasANumeros_saltoDeLinea_noUne() {
        //cada linea leida es un dato distinto: no debe quedar 72
        assertEquals("70\n2", ReglasOcr.palabrasANumeros("setenta\ndos"))
    }
}