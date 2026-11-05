package com.example.historialmedico.export

import android.content.Context
import android.graphics.Bitmap
import android.text.StaticLayout
import android.text.TextPaint
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.data.PresionArterial
import com.example.historialmedico.data.ReglasExamen
import com.example.historialmedico.util.Fechas
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    private const val LADO_MAXIMO_PX = 1500
    fun generarPdf(
        context: Context,
        paciente: Paciente,
        medicamentos: List<Medicamento>,
        horasMedicas: List<HoraMedica>,
        examenes: List<Examen>,
        presiones: List<PresionArterial>
    ): Uri {
        val documento = PdfDocument()
        val paintTitulo = TextPaint().apply { textSize = 20f; isFakeBoldText = true }
        val paintSubtitulo = TextPaint().apply { textSize = 14f; isFakeBoldText = true }
        val paintTexto = TextPaint().apply { textSize = 12f }

        val margenIzquierdo = 40f
        val altoMaximo = 800f
        val anchoTexto = 515
        var numeroPagina = 1

        var paginaInfo = PdfDocument.PageInfo.Builder(595, 842, numeroPagina).create()
        var pagina = documento.startPage(paginaInfo)
        var canvas = pagina.canvas
        var y = 40f

        fun nuevaPagina() {
            documento.finishPage(pagina)
            numeroPagina++
            paginaInfo = PdfDocument.PageInfo.Builder(595, 842, numeroPagina).create()
            pagina = documento.startPage(paginaInfo)
            canvas = pagina.canvas
            y = 40f
        }

        fun saltoDePaginaSiNecesario(espacio: Float = 0f) {
            if (y + espacio > altoMaximo) nuevaPagina()
        }

        //StaticLayout parte el texto en lineas; cada linea revisa el salto de pagina
        fun escribir(texto: String, paint: TextPaint, altoLinea: Float = 18f, sangria: Float = 0f) {
            val lineas = StaticLayout.Builder
                .obtain(texto, 0, texto.length, paint, anchoTexto - sangria.toInt())
                .build()
            for (i in 0 until lineas.lineCount) {
                saltoDePaginaSiNecesario()
                val linea = texto.substring(lineas.getLineStart(i), lineas.getLineEnd(i)).trimEnd()
                canvas.drawText(linea, margenIzquierdo + sangria, y, paint)
                y += altoLinea
            }
        }

        //reserva espacio para que el subtitulo no quede solo al final de la pagina
        fun seccion(titulo: String) {
            y += 12f
            saltoDePaginaSiNecesario(espacio = 38f)
            escribir(titulo, paintSubtitulo, altoLinea = 20f)
        }

        fun dibujarImagenSiExiste(uriString: String?) {
            if (uriString == null) return
            val bitmap = leerImagen(context, Uri.parse(uriString))
            if (bitmap == null) {
                //sin este aviso el PDF omitiria un documento sin que nadie lo note
                escribir("(Documento no disponible)", paintTexto)
                return
            }

            //limita tambien el alto: un escaneo largo no cabe aunque se ajuste el ancho
            val escala = minOf(500f / bitmap.width, (altoMaximo - 40f) / bitmap.height)
            val anchoFinal = bitmap.width * escala
            val altoFinal = bitmap.height * escala
            saltoDePaginaSiNecesario(altoFinal)
            val destino = android.graphics.RectF(
                margenIzquierdo,
                y,
                margenIzquierdo + anchoFinal,
                y + altoFinal
            )
            canvas.drawBitmap(bitmap, null, destino, null)
            y += altoFinal + 12f
        }
        escribir("Historial Medico - ${paciente.nombre}", paintTitulo, altoLinea = 24f)
        escribir("Relacion: ${paciente.relacion}", paintTexto)
        escribir("Generado el ${Fechas.fechaHoraVisible(System.currentTimeMillis())}", paintTexto)

        seccion("Medicamentos")
        if (medicamentos.isEmpty()) {
            escribir("Sin medicamentos registrados", paintTexto)
        } else {
            medicamentos.forEach { medicamento ->
                escribir("- ${medicamento.nombre} - ${medicamento.dosis}", paintTexto)
                dibujarImagenSiExiste(medicamento.documentoUri)
            }
        }
        seccion("Horas Medicas")
        if (horasMedicas.isEmpty()) {
            escribir("Sin horas medicas registradas", paintTexto)
        } else {
            horasMedicas.forEach { hora ->
                escribir(
                    "- ${hora.especialidad} - ${Fechas.fechaHoraVisible(hora.fechaHora)} (${hora.lugar})",
                    paintTexto
                )
            }
        }

        seccion("Examenes")
        if (examenes.isEmpty()) {
            escribir("Sin examenes registrados", paintTexto)
        } else {
            val consultasPorId = horasMedicas.associateBy { it.id }
            examenes.forEach { examen ->
                val detalle = listOfNotNull(
                    examen.fecha?.let { Fechas.isoAVisible(it) },
                    examen.resultado
                ).joinToString(" - ")
                escribir("- ${examen.tipo} (${examen.estado.etiqueta}) $detalle", paintTexto)
                examen.horaMedicaId?.let {
                    consultasPorId[it]
                }?.let { consulta ->
                    escribir(
                        "Solicitado en: ${ReglasExamen.textoConsulta(consulta)}",
                        paintTexto,
                        sangria = 12f
                    )
                }
                dibujarImagenSiExiste(examen.documentoUri)
            }
        }
        seccion("Presion arterial")
        if (presiones.isEmpty()) {
            escribir("Sin mediciones registradas", paintTexto)
        } else {
            presiones.forEach { presion ->
                val pulso = presion.pulso?.let { " - Pulso: $it lpm" } ?: ""
                escribir(
                    "- ${Fechas.fechaHoraVisible(presion.fechaHora)}:" +
                            " ${presion.sistolica}/${presion.diastolica} mmHg$pulso",
                    paintTexto
                )
            }
        }
        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "pdfs")
        borrarTemporales(context)
        carpeta.mkdirs()
        val nombreArchivo = paciente.nombre.replace(Regex("[^A-Za-z0-9]"), "_")
        val archivo = File(carpeta, "historial_$nombreArchivo.pdf")
        try {
            FileOutputStream(archivo).use { documento.writeTo(it) }
        } finally {
            documento.close()
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    //carga la imagen reducida: el PDF retiene cada imagen en memoria hasta escribirse
    private fun leerImagen(context: Context, uri: Uri): Bitmap? =
        try {
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, medidas)
            }
            val ladoMayor = maxOf(medidas.outWidth, medidas.outHeight)
            var muestreo = 1
            while (ladoMayor / (muestreo * 2) >= LADO_MAXIMO_PX) muestreo *= 2
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = muestreo
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            }
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }

    //el PDF trae datos de salud: solo queda el ultimo, hasta exportar otro o abrir de nuevo la app
    fun borrarTemporales(context: Context) {
        File(context.cacheDir, "pdfs").deleteRecursively()
    }
}