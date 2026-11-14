package com.example.historialmedico.data

import com.example.historialmedico.util.ReglasTexto
import com.example.historialmedico.util.ReglasTexto.CampoTexto

object ReglasPaciente {
    const val YO = "Yo"
    const val EDAD_MAXIMA = 120

    //opciones de la lista desplegable, se guarda el texto tal cual
    val RELACIONES = listOf(
        YO, "Madre", "Padre", "Hijo/a", "Pareja", "Hermano/a",
        "Abuelo/a", "Nieto/a", "Otro familiar", "Paciente"
    )

    //fechas en ISO "yyyy-MM-dd", comparar "MM-dd" como texto sirve porque van con dos digitos
    fun edad(fechaNacimiento: String, hoy: String): Int {
        val anios = hoy.substring(0, 4).toInt() - fechaNacimiento.substring(0, 4).toInt()
        //si este año todavia no llega a su cumpleaños tiene uno menos
        return if (hoy.substring(5) < fechaNacimiento.substring(5)) anios - 1 else anios
    }

    fun validarFechaNacimiento(fecha: String?, hoy: String): String? =
        when {
            fecha == null -> "Elija la fecha de nacimiento"
            fecha > hoy -> "La fecha de nacimiento no puede ser futura"
            edad(
                fecha,
                hoy
            ) > EDAD_MAXIMA -> "Revise la fecha de nacimiento: da más de $EDAD_MAXIMA años"

            else -> null
        }

    //revisa la ficha completa antes de guardar, otros son todos los pacientes del perfil
    fun validar(paciente: Paciente, otros: List<Paciente>, hoy: String): String? {
        //al editar, el mismo paciente no cuenta como repetido
        val resto = otros.filter { it.id != paciente.id }
        val nombre = ReglasTexto.normalizar(paciente.nombre)
        return ReglasTexto.validar(paciente.nombre, CampoTexto.NOMBRE_PERSONA)
            ?: (if (paciente.relacion !in RELACIONES) "Elija la relación" else null)
            ?: validarFechaNacimiento(paciente.fechaNacimiento, hoy)
            ?: when {
                paciente.sexo == null -> "Elija el sexo"
                paciente.estadoCivil == null -> "Elija el estado civil"
                paciente.prevision == null -> "Elija la previsión"
                paciente.grupoSanguineo == null -> "Elija el grupo sanguíneo"
                paciente.relacion == YO && resto.any { it.relacion == YO } -> "Ya hay un paciente registrado como $YO"
                resto.any {
                    it.relacion == paciente.relacion && ReglasTexto.normalizar(it.nombre)
                        .equals(nombre, ignoreCase = true)
                } -> "Ya existe un paciente con ese nombre y esa relación"

                else -> null
            }
    }
}