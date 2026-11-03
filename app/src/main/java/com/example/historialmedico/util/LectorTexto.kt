package com.example.historialmedico.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume

//una linea leida y su alto en pixeles (en una caja, la mas alta suele ser el nombre)
data class LineaLeida(val texto: String, val alto: Int)

object LectorTexto {
    //lee el texto de la imagen; null si no se pudo abrir o fallo
    suspend fun leer(context: Context, uri: Uri): List<LineaLeida>? {
        val imagen = withContext(Dispatchers.IO) {
            try {
                InputImage.fromFilePath(context, uri)
            } catch (e: IOException) {
                null
            }
        } ?: return null
        val lector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            suspendCancellableCoroutine { continuation ->
                lector.process(imagen)
                    .addOnSuccessListener { resultado ->
                        val lineas = resultado.textBlocks
                            .flatMap { it.lines }
                            .map { LineaLeida(it.text, it.boundingBox?.height() ?: 0) }
                        continuation.resume(lineas)
                    }
                    .addOnFailureListener { continuation.resume(null) }
            }
        } finally {
            lector.close()
        }
    }
}