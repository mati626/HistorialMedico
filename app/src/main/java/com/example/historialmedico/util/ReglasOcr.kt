package com.example.historialmedico.util

object ReglasOcr {
    //valor de cada palabra: con y sin tilde porque el OCR suele perderlas
    private val valores = mapOf(
        "cero" to 0, "un" to 1, "uno" to 1, "una" to 1, "dos" to 2, "tres" to 3,
        "cuatro" to 4, "cinco" to 5, "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9,
        "diez" to 10, "once" to 11, "doce" to 12, "trece" to 13, "catorce" to 14,
        "quince" to 15, "dieciseis" to 16, "dieciséis" to 16, "diecisiete" to 17,
        "dieciocho" to 18, "diecinueve" to 19, "veinte" to 20, "veintiun" to 21,
        "veintiún" to 21, "veintiuno" to 21, "veintiuna" to 21, "veintidos" to 22,
        "veintidós" to 22, "veintitres" to 23, "veintitrés" to 23, "veinticuatro" to 24,
        "veinticinco" to 25, "veintiseis" to 26, "veintiséis" to 26, "veintisiete" to 27,
        "veintiocho" to 28, "veintinueve" to 29, "treinta" to 30, "cuarenta" to 40,
        "cincuenta" to 50, "sesenta" to 60, "setenta" to 70, "ochenta" to 80,
        "noventa" to 90, "cien" to 100, "ciento" to 100, "doscientos" to 200,
        "trescientos" to 300, "cuatrocientos" to 400, "quinientos" to 500,
        "seiscientos" to 600, "setecientos" to 700, "ochocientos" to 800,
        "novecientos" to 900
    )

    private val espacios = Regex("\\s+")

    //una palabra completa: "dos" cuenta, pero no el "dos" de "dosis"
    private val palabraNumero =
        "(?<!\\p{L})(?:${(valores.keys + "mil").joinToString("|")})(?!\\p{L})"

    //palabras seguidas en la misma linea, con "y" opcional: "ciento treinta y dos"
    private val secuenciaNumero =
        Regex("$palabraNumero(?:[ \\t]+(?:y[ \\t]+)?$palabraNumero)*", RegexOption.IGNORE_CASE)

    //"quinientos mg" -> "500 mg"; el resto del texto queda igual
    fun palabrasANumeros(texto: String): String =
        secuenciaNumero.replace(texto) { numerosDe(it.value).joinToString(" ") }

    //una secuencia puede traer varios numeros: "ciento veinte ochenta" -> [120,80]
    private fun numerosDe(secuencia: String): List<Int> {
        val numeros = mutableListOf<Int>()
        var numero = 0
        var limite = -1 //cuanto puede valer la palabra siguiente; -1 = aun no empieza
        for (palabra in secuencia.lowercase().split(espacios)) {
            when (palabra) {
                "y" -> {}
                "mil" -> {
                    if (numero >= 1000) {
                        numeros.add(numero)
                        numero = 0
                    }
                    numero = maxOf(numero, 1) * 1000
                    limite = 999
                }

                else -> {
                    val valor = valores[palabra] ?: continue
                    if (limite >= 0 && valor !in 1..limite) {
                        numeros.add(numero)
                        numero = 0
                    }
                    numero += valor
                    limite = when {
                        valor == 0 -> 0
                        valor % 100 == 0 -> 99
                        valor >= 20 && valor % 10 == 0 -> 9
                        else -> 0
                    }
                }
            }
        }
        numeros.add(numero)
        return numeros
    }
}