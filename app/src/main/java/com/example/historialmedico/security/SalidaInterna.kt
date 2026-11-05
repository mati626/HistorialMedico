package com.example.historialmedico.security

//marca que la app abrio otra pantalla a proposito (escaner, visor, compartir, ajustes)
object SalidaInterna {
    var activa = false
        private set

    fun marcar(){
        activa=true
    }

    fun limpiar(){
        activa = false
    }
}