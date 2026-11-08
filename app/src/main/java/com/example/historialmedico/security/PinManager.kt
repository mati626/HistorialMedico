package com.example.historialmedico.security

import android.content.Context
import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PinManager(context: Context) {

    private val masterKey =
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasPinSet(): Boolean = prefs.contains(KEY_PIN_HASH)

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, HashPin.crear(pin)).apply()
    }

    //cuenta antes de comprobar: cerrar la app a mitad no regala el intento
    fun intentarPin(pin: String): Boolean {
        if (esperaRestante() > 0L) return false
        registrarIntento()
        val correcto = validatePin(pin)
        if (correcto) reiniciarIntentos()
        return correcto
    }

    fun esperaRestante(): Long {
        val fallidos = prefs.getInt(KEY_FALLIDOS, 0)
        val ultimo = prefs.getLong(KEY_ULTIMO_INTENTO, 0L)
        val ahora = SystemClock.elapsedRealtime()
        //este reloj vuelve a cero al reiniciar el celular: la espera parte de nuevo, nunca se acorta
        if (ahora < ultimo) {
            prefs.edit().putLong(KEY_ULTIMO_INTENTO, ahora).apply()
            return ReglasIntentos.espera(fallidos)
        }
        return ReglasIntentos.restante(fallidos, ahora - ultimo)
    }

    fun reiniciarIntentos() {
        prefs.edit().remove(KEY_FALLIDOS).remove(KEY_ULTIMO_INTENTO).apply()
    }

    private fun validatePin(pin: String): Boolean {
        val guardado = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val correcto = HashPin.coincide(pin, guardado)
        //el PIN ya esta confirmado: se deja en el formato nuevo
        if (correcto && HashPin.esAntiguo(guardado)) setPin(pin)
        return correcto
    }

    //commit y no apply: el intento debe quedar escrito antes de comprobar el PIN
    @SuppressLint("ApplySharedPref")
    private fun registrarIntento() {
        prefs.edit()
            .putInt(KEY_FALLIDOS, prefs.getInt(KEY_FALLIDOS, 0) + 1)
            .putLong(KEY_ULTIMO_INTENTO, SystemClock.elapsedRealtime())
            .commit()
    }

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_FALLIDOS = "pin_fallidos"
        private const val KEY_ULTIMO_INTENTO = "pin_ultimo_intento"
    }
}