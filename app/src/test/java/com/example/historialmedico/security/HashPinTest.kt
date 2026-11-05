package com.example.historialmedico.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HashPinTest {
    //SHA-256 de "1234", tal como lo guardaba la version anterior
    private val antiguo1234 = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"

    @Test
    fun coincide_pinCorrecto(){
        assertTrue(HashPin.coincide("1234", HashPin.crear("1234")))
    }

    @Test
    fun coincide_pinIncorrecto(){
        assertFalse(HashPin.coincide("4321", HashPin.crear("1234")))
    }

    @Test
    fun crear_mismoPin_daValoresDistintos(){
        assertNotEquals(HashPin.crear("1234"), HashPin.crear("1234"))
    }

    @Test
    fun coincide_formatoAntiguo_pinCorrecto(){
        assertTrue(HashPin.coincide("1234", antiguo1234))
    }

    @Test
    fun coincide_formatoAntiguo_pinIncorrecto(){
        assertFalse(HashPin.coincide("4321", antiguo1234))
    }

    @Test
    fun esAntiguo_distingueLosFormatos(){
        assertTrue(HashPin.esAntiguo(antiguo1234))
        assertFalse(HashPin.esAntiguo(HashPin.crear("1234")))
    }

    //vector publicado en RFC 6070: comprueba el algoritmo y que respete las iteraciones guardadas
    @Test
    fun coincide_vectorRfc6070(){
        val guardado = "1:73616c74:0c60c80f961f0e71f3a9b524af6012062fe037a6"
        assertTrue(HashPin.coincide("password", guardado))
    }
}
