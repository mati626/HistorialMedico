package com.example.historialmedico.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.Paciente
import com.example.historialmedico.util.Fechas
import java.io.File
import java.io.FileOutputStream

object PdfExporter{
    fun generarPdf(
        context: Context,
        paciente: Paciente,
        medicamentos: List<Medicamento>,
        horasMedicas: List<HoraMedica>,
        examenes: List<Examen>
    ): Uri{
        val documento= PdfDocument()
        val paintTitulo= Paint().apply { textSize=20f; isFakeBoldText=true }
        val paintSubtitulo=Paint().apply { textSize=14f; isFakeBoldText=true }
        val paintTexto=Paint().apply{textSize=12f}

        val margenIzquierdo=40f
        val altoMaximo=800f
        var numeroPagina=1

        var paginaInfo= PdfDocument.PageInfo.Builder(595,842,numeroPagina).create()
        var pagina=documento.startPage(paginaInfo)
        var canvas = pagina.canvas
        var y=40f

        fun saltoDePaginaSiNecesario(){
            if(y>altoMaximo){
                documento.finishPage(pagina)
                numeroPagina++
                paginaInfo= PdfDocument.PageInfo.Builder(595,842,numeroPagina).create()
                pagina=documento.startPage(paginaInfo)
                canvas=pagina.canvas
                y=40f
            }
        }

        fun dibujarImagenSiExiste(uriString: String?) {
            if (uriString == null) return
            val bitmap = try {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            } ?: return

            val anchoFinal = 500f
            val escala = anchoFinal / bitmap.width
            val altoFinal = bitmap.height * escala

            if (y + altoFinal > altoMaximo) {
                documento.finishPage(pagina)
                numeroPagina++
                paginaInfo = PdfDocument.PageInfo.Builder(595, 842, numeroPagina).create()
                pagina = documento.startPage(paginaInfo)
                canvas = pagina.canvas
                y = 40f
            }

            val destino = android.graphics.RectF(
                margenIzquierdo,
                y,
                margenIzquierdo + anchoFinal,
                y + altoFinal
            )
            canvas.drawBitmap(bitmap, null, destino, null)
            y += altoFinal + 12f
        }
        canvas.drawText("Historial Medico - ${paciente.nombre}",margenIzquierdo, y, paintTitulo)
        y+=24f
        canvas.drawText("Relacion: ${paciente.relacion}",margenIzquierdo,y,paintTexto)
        y+=30f
        canvas.drawText("Medicamentos", margenIzquierdo,y,paintSubtitulo)
        y+=20f
        if(medicamentos.isEmpty()){
            canvas.drawText("Sin medicamentos registrados",margenIzquierdo,y,paintTexto)
            y+=18f
        }else{
            medicamentos.forEach { medicamento ->
                saltoDePaginaSiNecesario()
                val texto="${medicamento.nombre} - ${medicamento.dosis}"
                canvas.drawText("- $texto",margenIzquierdo,y,paintTexto)
                y+=18f
                dibujarImagenSiExiste(medicamento.documentoUri)
            }
        }
        y+=12f

        saltoDePaginaSiNecesario()
        canvas.drawText("Horas Medicas",margenIzquierdo,y,paintSubtitulo)
        y+=20f
        if(horasMedicas.isEmpty()){
            canvas.drawText("Sin horas medicas registradas",margenIzquierdo,y,paintTexto)
            y+=18f
        }else{
            horasMedicas.forEach { hora->
                saltoDePaginaSiNecesario()
                canvas.drawText("- ${hora.especialidad} - ${Fechas.fechaHoraVisible(hora.fechaHora)} (${hora.lugar})",margenIzquierdo,y,paintTexto)
                y+=18f
            }
        }
        y+=12f

        saltoDePaginaSiNecesario()
        canvas.drawText("Examenes",margenIzquierdo,y,paintSubtitulo)
        y+=20f
        if(examenes.isEmpty()){
            canvas.drawText("Sin examenes registrados",margenIzquierdo,y,paintTexto)
            y+=18f
        }else{
            examenes.forEach{examen->
                saltoDePaginaSiNecesario()
                val detalle = listOfNotNull(
                    examen.fecha?.let { Fechas.isoAVisible(it) },
                    examen.resultado
                ).joinToString(" - ")
                canvas.drawText(
                    "- ${examen.tipo} (${examen.estado.etiqueta}) $detalle",
                    margenIzquierdo, y, paintTexto
                )
                y+=18f
                dibujarImagenSiExiste(examen.documentoUri)
        }
    }
        documento.finishPage(pagina)

        val carpeta=File(context.cacheDir,"pdfs")
        if(!carpeta.exists()) carpeta.mkdirs()
        val nombreArchivo=paciente.nombre.replace(Regex("[^A-Za-z0-9]"),"_")
        val archivo=File(carpeta,"historial_$nombreArchivo.pdf")
        FileOutputStream(archivo).use { documento.writeTo(it) }
        documento.close()

        return FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",archivo)
}
}