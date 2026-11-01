package com.example.historialmedico.data

object ReglasPastillas {
    const val CANTIDAD_MAXIMA = 999
    const val UNIDADES_MAXIMAS = 10
    const val DIAS_AVISO = 5

    //division entera: un dia que no alcanza completo no se cuenta
    fun diasRestantes(cantidad: Int, unidadesPorToma: Int, tomasPorDia: Int): Int? {
        val porDia = unidadesPorToma * tomasPorDia
        return if (porDia <= 0) null else cantidad / porDia
    }

    fun quedanPocas(dias: Int?): Boolean = dias != null && dias <= DIAS_AVISO

    //actual=pastillas que ya tiene, para no pasarse del maximo al reponer
    fun validarCantidad(cantidad: Int?, actual: Int = 0): String? = when {
        cantidad == null -> "Ingrese la cantidad de pastillas"
        cantidad !in 1..CANTIDAD_MAXIMA -> "La cantidad debe estar entre 1 y $CANTIDAD_MAXIMA"
        actual + cantidad > CANTIDAD_MAXIMA -> "No puede tener más de $CANTIDAD_MAXIMA pastillas"
        else -> null
    }

    fun validarUnidadesPorToma(unidades: Int?): String? = when {
        unidades == null -> "Ingrese cuántas pastillas toma cada vez"
        unidades !in 1..UNIDADES_MAXIMAS -> "Las pastillas por toma deben estar entre 1 y $UNIDADES_MAXIMAS"
        else -> null
    }

    fun textoRestantes(cantidad: Int, dias: Int?): String = when {
        cantidad == 0 -> "Pastillas: 0 (se acabaron)"
        dias == null -> "Pastillas: $cantidad"
        dias == 0 -> "Pastillas: $cantidad (no alcanzan para un día)"
        dias == 1 -> "Pastillas: $cantidad (alcanzan para 1 día)"
        else -> "Pastillas: $cantidad (alcanzan para $dias días)"
    }
}