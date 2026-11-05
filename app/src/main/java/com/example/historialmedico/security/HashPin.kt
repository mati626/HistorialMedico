package com.example.historialmedico.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

//convierte el PIN en un valor que no se puede revertir, separado para probar
object HashPin {
    //SHA256 exige API 26 y la app parte en API 24; las colisiones de SHA-1 no afectan a PBKDF2
    private const val ALGORITMO = "PBKDF2WithHmacSHA1"
    private const val ITERACIONES = 120_000

    //160 es la salida de SHA-1: pedir mas hace trabajar de mas a la app, no a un atacante
    private const val BITS_HASH = 160
    private const val BYTES_SAL = 16

    fun crear(pin: String): String {
        val sal = ByteArray(BYTES_SAL)
        SecureRandom().nextBytes(sal)
        return "$ITERACIONES:${aHex(sal)}:${aHex(derivar(pin, sal, ITERACIONES))}"
    }

    fun coincide(pin: String, guardado: String): Boolean {
        if (esAntiguo(guardado))
            return iguales(hashAntiguo(pin), guardado)
        val (iteraciones, sal, hash) = guardado.split(":")
        return iguales(aHex(derivar(pin, deHex(sal), iteraciones.toInt())), hash)
    }

    //antes se guardaba un SHA-256 sin sal, que no lleva separadores
    fun esAntiguo(guardado: String): Boolean = ':' !in guardado

    private fun derivar(pin: String, sal: ByteArray, iteraciones: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), sal, iteraciones, BITS_HASH)
        return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
    }

    private fun hashAntiguo(pin: String): String =
        aHex(MessageDigest.getInstance("SHA-256").digest(pin.toByteArray()))

    //isEqual tarda lo mismo sin importar donde difieran los valores
    private fun iguales(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(),b.toByteArray())

    private fun aHex(bytes: ByteArray): String = bytes.joinToString(""){
        "%02x".format(it)
    }

    private fun deHex(hex: String): ByteArray = hex.chunked(2).map {
        it.toInt(16).toByte()
    }.toByteArray()
}