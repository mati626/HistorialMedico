package com.example.historialmedico.security

//cuantos intentos de PIN se permiten y cuanto esperar despues, separado para probar
object ReglasIntentos {
    const val INTENTOS_LIBRES = 5

    //nunca bloquea para siempre: no hay forma de recuperar el PIN
    fun espera(fallidos: Int): Long = when{
        fallidos < INTENTOS_LIBRES->0L
        fallidos == INTENTOS_LIBRES->30_000L
        fallidos == INTENTOS_LIBRES+1->60_000L
        fallidos == INTENTOS_LIBRES+2->300_000L
        else->1_800_000L
    }

    fun restante(fallidos: Int, transcurrido: Long): Long =
        (espera(fallidos)-transcurrido).coerceAtLeast(0L)

    //redondea hacia arriba para no decir "0 segundos" mientras aun falta
    fun texto(restante: Long): String{
        val segundos = (restante +999)/1000
        if(segundos<60) return if(segundos==1L)
            "Espere 1 segundo" else "Espere $segundos segundos"
        val minutos = (segundos +59)/60
        return if(minutos==1L)"Espere 1 minuto" else "Espere $minutos minutos"
    }
}