package com.example.historialmedico.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthManager (private val activity: FragmentActivity){
    //uso de biometria (huella/rostro)
    fun canAuthenticate(): Boolean{
        val biometricManager= BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )== BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess:()-> Unit,
        onError: (errorCode: Int,errString: CharSequence)-> Unit,
        onFailed:()-> Unit
    ){
        val executor= ContextCompat.getMainExecutor(activity)
        val callback=object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ){
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode,errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }
        val biometricPrompt= BiometricPrompt(activity,executor,callback)
        val promptInfo= BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}