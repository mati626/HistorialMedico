package com.example.historialmedico.util

//reglas de texto compartidas por todos los formularios
object ReglasTexto {
    //caracteres aceptados en cada campo
    enum class TipoTexto(val permitidos: String) {
        NOMBRE("letras, espacios, ' y -"),
        GENERAL("letras, números, espacios y . , ' - / ( ) % # °"),
        LIBRE("caracteres visibles")
    }

    //tabla de reglas aprobada
    enum class CampoTexto(val etiqueta: String, val maximo: Int, val tipo: TipoTexto) {
        NOMBRE_PERSONA("Nombre", 50, TipoTexto.NOMBRE),
        ESPECIALIDAD("Especialidad", 50, TipoTexto.NOMBRE),
        RELACION("Relación", 20, TipoTexto.GENERAL),
        MEDICAMENTO("Nombre del medicamento", 50, TipoTexto.GENERAL),
        DOSIS("Dosis", 30, TipoTexto.GENERAL),
        LUGAR("Lugar", 80, TipoTexto.GENERAL),
        TIPO_EXAMEN("Tipo de examen", 60, TipoTexto.GENERAL),
        RESULTADO("Resultado", 300, TipoTexto.LIBRE)
    }

    private val espacios = Regex("\\s+")

    //quita los espacios de los extremos y deja uno entre las palabras
    fun normalizar(texto: String): String = texto.trim().replace(espacios, " ")

    //devuelve el mensaje de error, o null si el texto se puede guardar
    fun validar(texto: String, campo: CampoTexto, obligatorio: Boolean = true): String? {
        val limpio = normalizar(texto)
        return when {
            limpio.isEmpty() && obligatorio -> "El campo ${campo.etiqueta} es obligatorio"
            limpio.length > campo.maximo -> "El campo ${campo.etiqueta} admite máximo ${campo.maximo} caracteres"
            !limpio.all {
                esPermitido(
                    it,
                    campo.tipo
                )
            } -> "El campo ${campo.etiqueta} solo admite ${campo.tipo.permitidos}"

            else -> null
        }
    }

    private fun esPermitido(c: Char, tipo: TipoTexto): Boolean = when (tipo) {
        TipoTexto.NOMBRE -> c.isLetter() || c in " '-"
        TipoTexto.GENERAL -> c.isLetterOrDigit() || c in " .,'-/()%#°"
        TipoTexto.LIBRE -> !c.isISOControl()
    }
}
