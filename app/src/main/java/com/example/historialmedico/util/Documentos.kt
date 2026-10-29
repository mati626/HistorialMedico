package com.example.historialmedico.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

object Documentos {
    //copia el escaneo del ML kit al almacenamiento interno (queda en cache y android puede borrarlo)
    suspend fun guardar(context: Context, uriEscaneo: String): String? =
        withContext(Dispatchers.IO) {
            val carpeta = File(context.filesDir, "documentos")
            carpeta.mkdirs()
            val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
            try {
                context.contentResolver.openInputStream(Uri.parse(uriEscaneo))?.use { entrada ->
                    destino.outputStream().use { salida ->
                        entrada.copyTo(salida)
                    }
                } ?: throw IOException("No se pudo abrir el escaneo")
                archivo(uriEscaneo)?.delete()
                Uri.fromFile(destino).toString()
            } catch (e: IOException) {
                destino.delete()
                null
            }
        }

    suspend fun borrar(uriDocumento: String) {
        withContext(Dispatchers.IO) { archivo(uriDocumento)?.delete() }
    }

    //abre la imagen con la app del telefono o avisa si ya no esta
    fun abrir(context: Context, uriDocumento: String) {
        val archivo = archivo(uriDocumento)
        if (archivo == null || !archivo.exists()) {
            Toast.makeText(context, "El documento ya no está disponible", Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", archivo
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app para ver imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun archivo(uriDocumento: String): File? =
        Uri.parse(uriDocumento).path?.let { File(it) }
}