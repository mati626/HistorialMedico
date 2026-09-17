package com.example.historialmedico.security

import android.content.Context
import java.security.MessageDigest
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
class PinManager (context: Context){

    private val masterKey= MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs= EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun hasPinSet(): Boolean=prefs.contains(KEY_PIN_HASH)

    fun setPin(pin: String){
        prefs.edit().putString(KEY_PIN_HASH,hash(pin)).apply()
    }

    fun validatePin(pin: String): Boolean=
        prefs.getString(KEY_PIN_HASH,null)==hash(pin)

    private fun hash(value: String): String{
        val digest=
            MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") {"%02x".format(it)  }
    }

    companion object{
        private const val KEY_PIN_HASH="pin_hash"
    }
}