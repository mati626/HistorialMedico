package com.example.historialmedico.util

import android.content.Context
import android.net.Uri
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

    private fun archivo(uriDocumento: String): File? =
        Uri.parse(uriDocumento).path?.let { File(it) }
}