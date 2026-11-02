package com.example.historialmedico.data

object ReglasPresion {
    const val SISTOLICA_MIN = 50
    const val SISTOLICA_MAX = 300
    const val DIASTOLICA_MIN = 30
    const val DIASTOLICA_MAX = 200
    const val PULSO_MIN = 30
    const val PULSO_MAX = 250

    //devuelve el mensaje de error, o null si se puede guardar; pulso null = sin pulso
    fun validar(sistolica: Int?, diastolica: Int?, pulso: Int?): String? = when {
        sistolica == null -> "La presion sistolica es obligatoria"
        diastolica == null -> "La presion diastolica es obligatoria"
        sistolica !in SISTOLICA_MIN..SISTOLICA_MAX -> "La sistolica debe estar entre $SISTOLICA_MIN y $SISTOLICA_MAX"
        diastolica !in DIASTOLICA_MIN..DIASTOLICA_MAX -> "La diastolica debe estar entre $DIASTOLICA_MIN y $DIASTOLICA_MAX"
        sistolica <= diastolica -> "La sistolica debe ser mayor que la diastolica"
        pulso != null && pulso !in PULSO_MIN..PULSO_MAX -> "El pulso debe estar entre $PULSO_MIN y $PULSO_MAX"
        else -> null
    }
}