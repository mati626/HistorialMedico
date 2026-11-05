package com.example.historialmedico.security

//cuando volver a pedir el PIN al regresar a la app, separado de la pantalla para probar
object ReglasBloqueo {
    const val ESPERA_NORMAL = 30_000L

    //el escaner y las apps abiertas desde aqui tardan mas, y bloquear haria perder el escaneado
    const val ESPERA_SALIDA_INTERNA = 600_000L

    fun debeBloquear(transcurrido: Long, salidaInterna: Boolean): Boolean =
        transcurrido>=if(salidaInterna)
            ESPERA_SALIDA_INTERNA else ESPERA_NORMAL
}