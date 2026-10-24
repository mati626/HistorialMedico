package com.example.historialmedico.data

object ReglasExamen {
    fun estado(realizado: Boolean, fecha: String?): EstadoExamen=when{
        realizado-> EstadoExamen.REALIZADO
        fecha != null -> EstadoExamen.AGENDADO
        else -> EstadoExamen.SOLICITADO
    }

    fun validarFecha(realizado: Boolean, fecha: String?, hoy: String): String? = when {
        fecha == null -> if (realizado) "Indique la fecha en que se realizó el examen" else null
        realizado && fecha > hoy -> "Un examen realizado no puede tener fecha futura"
        !realizado && fecha < hoy -> "Un examen próximo no puede tener fecha pasada"
        else -> null
    }
}