package com.example.historialmedico.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

//clave con la que SQLCipher cifra la base de datos
//no sale del PIN porque las alarmas y el reinicio del celular abren la bd sin que nadie lo ingrese
//es un numero al azar guardado cifrado con una clave del Keystore, que nunca sale del celular
object ClaveBaseDatos {
    private const val ALIAS_KEYSTORE = "clave_bd"
    //prefs propias y no auth_prefs: las pruebas E2E borran auth_prefs y se perderia la clave
    private const val PREFS = "bd_prefs"
    private const val CLAVE_CIFRADA = "clave_cifrada"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val LARGO_IV = 12
    private const val BITS_TAG = 128

    //syncronized: si dos partes de la app piden a la vez no se crean dos claves distintas
    @Synchronized
    fun obtener(context: Context): ByteArray{
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val guardada = prefs.getString(CLAVE_CIFRADA, null)
        if (guardada != null) return descifrar(Base64.decode(guardada, Base64.NO_WRAP))

        val azar = ByteArray(32)
        SecureRandom().nextBytes(azar)
        //en hexadecimal quedan 64 letras y numeros, asi la clave se puede escribir dentro de una instruccion SQL
        val clave = azar.joinToString("") {"%02x".format(it)}.toByteArray()
        val cifrada = Base64.encodeToString(cifrar(clave), Base64.NO_WRAP)
        //commit y no apply: la clave tiene que quedar escrita antes de crear la base con ella
        check(prefs.edit().putString(CLAVE_CIFRADA, cifrada).commit()){
            "No se pudo guardar la clave de la base de datos"
        }
        return clave
    }

    //sin pedir huella ni desbloqueo: las alarmas leen la base con el celular bloqueado
    private fun claveKeystore(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existente = keyStore.getKey(ALIAS_KEYSTORE, null) as? SecretKey
        if (existente != null) return existente

        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generador.init(
            KeyGenParameterSpec.Builder(
                ALIAS_KEYSTORE,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generador.generateKey()
    }

    //el IV va delante del texto cifrado porque se necesita para descifrar
    private fun cifrar(datos: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.ENCRYPT_MODE, claveKeystore())
        return cipher.iv + cipher.doFinal(datos)
    }

    private fun descifrar(datos: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            claveKeystore(),
            GCMParameterSpec(BITS_TAG, datos, 0, LARGO_IV)
        )
        return cipher.doFinal(datos, LARGO_IV, datos.size - LARGO_IV)
    }
}