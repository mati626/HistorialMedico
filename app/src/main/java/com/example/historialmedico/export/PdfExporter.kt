package com.example.historialmedico.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.historialmedico.data.Examen
import com.example.historialmedico.data.HoraMedica
import com.example.historialmedico.data.Medicamento
import com.example.historialmedico.data.Perfil
import java.io.File
import java.io.FileOutputStream

object PdfExporter{
    fun generarPdf(
        context: Context,
        perfil: Perfil,
        medicamento: List<Medicamento>,
        horasMedicas: List<HoraMedica>,
        examenes: List<Examen>
    ): Uri{
        val documento= PdfDocument()
        val paintTitulo= Paint().apply { textSize=20f; isFakeBoldText=true }
        val paintSubtitulo=Paint().apply { textSize=14f; isFakeBoldText=true }
        val painTexto=Paint().apply{textSize=12f}

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

        canvas.drawText("Historial Medico -${perfil.nombre}",margenIzquierdo, y, paintTitulo)
        y+=24f
        canvas.drawText("Relacion: ${perfil.relacion}",margenIzquierdo,y,painTexto)
        y+=30f
        canvas.drawText("Medicamentos", margenIzquierdo,y,paintSubtitulo)
        y+=20f
        if(medicamento.isEmpty()){
            canvas.drawText("Sin medicamentos registrados",margenIzquierdo,y,painTexto)
            y+=18f
        }else{
            medicamento.forEach { medicamento ->
                saltoDePaginaSiNecesario()
                val texto=if(medicamento.dosis.isBlank()) medicamento.nombre else
                    "${medicamento.nombre}-${medicamento.dosis}"
                canvas.drawText("- $texto",margenIzquierdo,y,painTexto)
                y+=18f
            }
        }
        y+=12f

        saltoDePaginaSiNecesario()
        canvas.drawText("Horas Medicas",margenIzquierdo,y,painTexto)
        y+=20f
        if(horasMedicas.isEmpty()){
            canvas.drawText("Sin horas medicas registradas",margenIzquierdo,y,painTexto)
            y+=18f
        }else{
            horasMedicas.forEach { hora->
                saltoDePaginaSiNecesario()
                canvas.drawText("-${hora.especialidad}-${hora.fecha} (${hora.lugar})",margenIzquierdo,y,painTexto)
                y+=18f
            }
        }
        y+=12f

        saltoDePaginaSiNecesario()
        canvas.drawText("Examenes",margenIzquierdo,y,paintSubtitulo)
        y+=20f
        if(examenes.isEmpty()){
            canvas.drawText("Sin examenes registrados",margenIzquierdo,y,painTexto)
            y+=18f
        }else{
            examenes.forEach{examen->
                saltoDePaginaSiNecesario()
                canvas.drawText("-${examen.tipo}-${examen.fecha}(${examen.resultado})",
                    margenIzquierdo, y,painTexto)
                y+=18f
        }
    }
        documento.finishPage(pagina)

        val carpeta=File(context.cacheDir,"pdfs")
        if(!carpeta.exists()) carpeta.mkdirs()
        val nombreArchivo=perfil.nombre.replace(Regex("[^A-Za-z0-9]"),"_")
        val archivo=File(carpeta,"historial_$nombreArchivo.pdf")
        FileOutputStream(archivo).use { documento.writeTo(it) }
        documento.close()

        return FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",archivo)
}
}