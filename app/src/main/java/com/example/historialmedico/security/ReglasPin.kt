package com.example.historialmedico.security

//reglas para crear PIN nuevo, separadas de la pantalla para probar
object ReglasPin{
    const val LARGO_MINIMO = 4

    //devuelve el mensaje de error, o null si el PIN se puede guardar
    fun validarNuevoPin(pin: String, confirmacion: String): String? =
        when{
            pin.length < LARGO_MINIMO -> "El PIN debe tener mínimo $LARGO_MINIMO dígitos"
            pin != confirmacion -> "Los PIN no coinciden"
            else -> null
        }
}
