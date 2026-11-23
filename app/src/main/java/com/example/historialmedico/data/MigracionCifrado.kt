package com.example.historialmedico.data

import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.io.RandomAccessFile

//las versiones anteriores de la app guardaban la base de datos sin cifrar, aqui se pasa a cifrada sin perder datos
object MigracionCifrado {
    //cualquier archivo SQLite sin cifrar empieza con este texto, uno cifrado empieza con bytes al azar
    private val CABECERA_SIN_CIFRAR = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    fun cifrarSiHaceFalta(archivo: File, clave: ByteArray) {
        val temporal = File(archivo.path + ".cifrando")

        //si la app se cerro despues de borrar la bd vieja, la copia cifrada ya estaba completa
        if (!archivo.exists()) {
            if (temporal.exists()) reemplazar(archivo, temporal)
            return
        }
        //si la app se cerro a mitad de la copia, la copia quedo incompleta y se hace de nuevo
        SQLiteDatabase.deleteDatabase(temporal)

        if (!estaSinCifrar(archivo)) return
        copiarCifrada(archivo, temporal, clave)
        reemplazar(archivo, temporal)
    }

    internal fun estaSinCifrar(archivo: File): Boolean {
        if (archivo.length() < CABECERA_SIN_CIFRAR.size) return false
        val inicio = ByteArray(CABECERA_SIN_CIFRAR.size)
        RandomAccessFile(archivo, "r").use { it.readFully(inicio) }
        return inicio.contentEquals(CABECERA_SIN_CIFRAR)
    }

    //abre la base vieja sin clave y copia todo a un archivo nuevo con clave
    private fun copiarCifrada(archivo: File, temporal: File, clave: ByteArray) {
        //ATTACH abre el archivo nuevo con los mismos permisos de la base vieja, sin CREATE_IF_NECESSARY no lo puede crear
        val base = SQLiteDatabase.openDatabase(
            archivo.path,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
        )
        //rawExecSQL no avisa si algo falla, con execSQL y rawQuery un error corta todo antes de borrar la base vieja
        try {
            base.execSQL("ATTACH DATABASE ? AS cifrada KEY ?", arrayOf<Any>(temporal.path, clave))
            //sqlcipher_export es un SELECT, por eso va con rawQuery, y moveToFirst es lo que lo ejecuta
            base.rawQuery("SELECT sqlcipher_export('cifrada')").use { it.moveToFirst() }
            //sqlcipher_export no copia la version y Room la usa para saber que migraciones aplicar
            base.execSQL("PRAGMA cifrada.user_version = ${base.version}")
            base.execSQL("DETACH DATABASE cifrada")
        } finally {
            base.close()
        }
    }

    //borra la base vieja junto con sus archivos -wal y -shm y deja la cifrada en su lugar
    private fun reemplazar(archivo: File, temporal: File) {
        SQLiteDatabase.deleteDatabase(archivo)
        check(temporal.renameTo(archivo)) { "No se pudo dejar la base cifrada en su lugar" }
    }
}