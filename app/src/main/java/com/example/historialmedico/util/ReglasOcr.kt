package com.example.historialmedico.util

import com.example.historialmedico.data.ReglasPastillas
import com.example.historialmedico.data.ReglasPresion

//lo que se pudo leer de una caja o receta; null = no se encontro
data class MedicamentoLeido(val nombre: String?, val dosis: String?, val cantidad: Int?)

//lo que se pudo leer de un papel; null = no se encontro
data class PresionLeida(val sistolica: Int?, val diastolica: Int?, val pulso: Int?)

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

    //numero + unidad: "50 mg", "2,5 mg", "500 miligramos"
    private val dosisRegex = Regex(
        "(\\d+(?:[.,]\\d+)?)\\s*(miligramos?|microgramos?|mililitros?|gramos?|mcg|mg|ug|ml|ui|g|%)(?!\\p{L})",
        RegexOption.IGNORE_CASE
    )

    //la unidad como se escribe en el formulario
    private fun unidadCorta(unidad: String): String = when (unidad.lowercase()) {
        "miligramo", "miligramos" -> "mg"
        "microgramo", "microgramos", "ug" -> "mcg"
        "mililitro", "mililitros" -> "ml"
        "gramo", "gramos" -> "g"
        "ui" -> "UI"
        else -> unidad.lowercase()
    }

    private fun dosisDe(texto: String): String? {
        val encontrada = dosisRegex.find(texto) ?: return null
        val (numero, unidad) = encontrada.destructured
        val corta = unidadCorta(unidad)
        val dosis = if (corta == "%") "$numero%" else "$numero $corta"
        return dosis.takeIf { ReglasTexto.validar(it, ReglasTexto.CampoTexto.DOSIS) == null }
    }

    //"30 comprimidos", "x 30 comp.", "14 cápsulas"
    private val cantidadRegex = Regex(
        "(?<![\\d.,])(\\d{1,4})\\s*(?:comprimidos?|comp\\.?|tabletas?|tab\\.?|c[aá]psulas?|caps?\\.?|grageas?|p[ií]ldoras?|pastillas?)(?!\\p{L})",
        RegexOption.IGNORE_CASE
    )

    //si hay varias, la mayor: "tomar 1 comprimido" no debe ganarle a "28 comprimidos"
    private fun cantidadDe(lineas: List<LineaLeida>): Int? =
        lineas.flatMap { linea ->
            cantidadRegex.findAll(linea.texto).map { it.groupValues[1].toInt() }.toList()
        }
            .filter { it in 1..ReglasPastillas.CANTIDAD_MAXIMA }
            .maxOrNull()

    private val noPermitidosNombre = Regex("[^\\p{L}\\d '-]")
    private val inicioSinLetras = Regex("^[^\\p{L}]+")

    //"Rp: 1. LOSARTAN POTASICO" -> "Losartan potasico"; null si no parece un nombre
    private fun limpiarNombre(texto: String): String? {
        val limpio = ReglasTexto.normalizar(
            texto.substringAfterLast(':')
                .replace(noPermitidosNombre, " ")
                .replace(inicioSinLetras, "")
        ).trimEnd('-', '\'', ' ')
        if (limpio.count { it.isLetter() } < 3) return null
        if (ReglasTexto.validar(limpio, ReglasTexto.CampoTexto.MEDICAMENTO) != null) return null
        if (limpio != limpio.uppercase()) return limpio
        //todo en mayusculas (comun en cajas): queda como oracion, sin tocar palabras como "D3"
        return limpio.split(" ")
            .joinToString(" ") { palabra ->
                if (palabra.all { it.isLetter() }) palabra.lowercase() else palabra
            }
            .replaceFirstChar { it.titlecase() }
    }

    private fun nombreDe(lineas: List<LineaLeida>): String? {
        //1) en la linea de la dosis, lo que va antes: "Losartán potásico 50 mg"
        for (linea in lineas) {
            val dosis = dosisRegex.find(linea.texto) ?: continue
            limpiarNombre(linea.texto.substring(0, dosis.range.first))?.let { return it }
        }
        //2) si no, la linea con letras mas grandes que no sea dosis ni cantidad
        return lineas
            .filter { !dosisRegex.containsMatchIn(it.texto) && !cantidadRegex.containsMatchIn(it.texto) }
            .sortedByDescending { it.alto }
            .firstNotNullOfOrNull { limpiarNombre(it.texto) }
    }

    //nombre, dosis y cantidad desde las lineas leidas de una caja o receta
    fun leerMedicamento(lineas: List<LineaLeida>): MedicamentoLeido {
        val convertidas = lineas.map { it.copy(texto = palabrasANumeros(it.texto)) }
        return MedicamentoLeido(
            nombre = nombreDe(convertidas),
            dosis = convertidas.firstNotNullOfOrNull { dosisDe(it.texto) },
            cantidad = cantidadDe(convertidas)
        )
    }

    private val sobre = Regex("(?<!\\p{L})sobre(?!\\p{L})", RegexOption.IGNORE_CASE)

    //"120/80" o "120 / 80"
    private val sistolicaSobreDiastolica = Regex("(?<!\\d)(\\d{2,3})\\s*/\\s*(\\d{2,3})(?!\\d)")

    //etiqueta y numero en la misma linea: "Sistolica 120", "SYS. 120", "Pulso: 72"
    private fun conEtiqueta(etiquetas: String) = Regex(
        "(?<!\\p{L})(?:$etiquetas)[.:= \\t]*(\\d{2,3})(?!\\d)",
        RegexOption.IGNORE_CASE
    )

    private val sistolicaConEtiqueta = conEtiqueta("sist[oó]lica|sist|sys")
    private val diastolicaConEtiqueta = conEtiqueta("diast[oó]lica|diast|dia")
    private val pulsoConEtiqueta = conEtiqueta("pulso|pulse|pul")

    private fun valorDe(etiqueta: Regex, texto: String): Int? =
        etiqueta.find(texto)?.groupValues?.get(1)?.toInt()


    //el OCR puede confundir cifras con letras: "I2O" -> "120"
    private val cifrasConLetras =
        Regex("(?<![\\p{L}\\d])[\\dOoIlSB]*\\d[\\dOoIlSB]*(?![\\p{L}\\d])")

    private fun corregirCifras(texto: String): String =
        cifrasConLetras.replace(texto) { encontrado ->
            encontrado.value.map {
                when (it) {
                    'O', 'o' -> '0'
                    'I', 'l' -> '1'
                    'S' -> '5'
                    'B' -> '8'
                    else -> it
                }
            }.joinToString("")
        }

    //sistolica, diastolica y pulso desde las lineas leidas de un papel
    fun leerPresion(lineas: List<LineaLeida>): PresionLeida {
        val texto = lineas.joinToString("\n") {
            corregirCifras(palabrasANumeros(it.texto))
        }.replace(sobre, "/")
        //1. escrita como 120/80: el primer par que el formulario aceptaria
        val pareja = sistolicaSobreDiastolica.findAll(texto).firstOrNull {
            ReglasPresion.validar(
                it.groupValues[1].toInt(),
                it.groupValues[2].toInt(),
                null
            ) == null
        }
        //2. si no, con etiquetas en la misma linea; nunca por posicion
        val sistolica = pareja?.groupValues?.get(1)?.toInt()
            ?: valorDe(sistolicaConEtiqueta, texto)
        val diastolica = pareja?.groupValues?.get(2)?.toInt()
            ?: valorDe(diastolicaConEtiqueta, texto)
        if (ReglasPresion.validar(sistolica, diastolica, null) != null) {
            return PresionLeida(null, null, null)
        }
        val pulso = valorDe(pulsoConEtiqueta, texto)?.takeIf {
            ReglasPresion.validar(sistolica, diastolica, it) == null
        }
        return PresionLeida(sistolica, diastolica, pulso)
    }

}