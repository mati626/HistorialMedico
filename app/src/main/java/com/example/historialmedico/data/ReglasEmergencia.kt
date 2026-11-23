package com.example.historialmedico.data

import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto

//reglas de la seccion Para emergencias del paciente
object ReglasEmergencia {
    //un celular chileno con +56 tiene 11 digitos y sin codigo 9, el maximo internacional es 15
    const val DIGITOS_MINIMOS = 8
    const val DIGITOS_MAXIMOS = 15

    //todo es opcional, pero un contacto sin telefono o sin nombre no sirve en una emergencia
    fun validar(paciente: Paciente): String? =
        ReglasTexto.validar(paciente.alergias.orEmpty(), CampoTexto.ALERGIAS, obligatorio = false)
            ?: ReglasTexto.validar(paciente.enfermedades.orEmpty(), CampoTexto.ENFERMEDADES, obligatorio = false)
            ?: validarContacto(paciente.contactoNombre.orEmpty(), paciente.contactoTelefono.orEmpty())

    fun validarContacto(nombre: String, telefono: String): String? = when {
        nombre.isBlank() && telefono.isBlank() -> null
        nombre.isBlank() -> "Falta el nombre del contacto de emergencia"
        telefono.isBlank() -> "Falta el teléfono del contacto de emergencia"
        else -> ReglasTexto.validar(nombre, CampoTexto.CONTACTO) ?: validarTelefono(telefono)
    }

    //el + solo puede ir al inicio, los espacios y guiones se aceptan porque asi se escriben los numeros
    fun validarTelefono(telefono: String): String? {
        val limpio = telefono.trim()
        val digitos = limpio.count { it.isDigit() }
        return when {
            !limpio.removePrefix("+").all { it.isDigit() || it in " -" } ->
                "El teléfono solo admite números, espacios, guiones y + al inicio"

            digitos !in DIGITOS_MINIMOS..DIGITOS_MAXIMOS ->
                "El teléfono debe tener entre $DIGITOS_MINIMOS y $DIGITOS_MAXIMOS dígitos"

            else -> null
        }
    }

    //lo que recibe el marcador del telefono: el + del inicio y los numeros, sin espacios ni guiones
    fun paraMarcar(telefono: String): String {
        val digitos = telefono.filter { it.isDigit() }
        return if (telefono.trim().startsWith("+")) "+$digitos" else digitos
    }
}