package com.example.historialmedico.security

//reglas para crear PIN nuevo, separadas de la pantalla para probar
//una funcion por campo, asi la pantalla sabe bajo cual campo va cada error
object ReglasPin{
    const val LARGO_MINIMO = 4
    const val LARGO_MAXIMO = 6

    //error del primer campo, o null si el largo esta bien
    fun errorDeLargo(pin: String): String? =
        if (pin.length < LARGO_MINIMO) "El PIN debe tener mínimo $LARGO_MINIMO dígitos" else null

    //error del campo de confirmacion, o null si los dos PIN son iguales
    fun errorDeConfirmacion(pin: String, confirmacion: String): String? =
        if (pin != confirmacion) "Los PIN no coinciden" else null
}