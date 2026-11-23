package com.example.historialmedico.data

import android.database.sqlite.SQLiteDatabase as SQLiteAndroid
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

//pasar la base sin cifrar a cifrada es lo unico que toca datos reales al actualizar la app
@RunWith(AndroidJUnit4::class)
class MigracionCifradoTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    //archivo de prueba aparte, la base de la app no se toca
    private val archivo = context.getDatabasePath("prueba_migracion.db")
    private val temporal = File(archivo.path + ".cifrando")
    private val clave = "clave-de-prueba".toByteArray()

    @Before
    fun preparar() {
        System.loadLibrary("sqlcipher")
        borrarArchivos()
    }

    @After
    fun limpiar() {
        borrarArchivos()
    }

    @Test
    fun bdSinCifrar_quedaCifradaConSusDatos() {
        crearSinCifrar()
        assertTrue(MigracionCifrado.estaSinCifrar(archivo))

        MigracionCifrado.cifrarSiHaceFalta(archivo, clave)

        assertFalse(MigracionCifrado.estaSinCifrar(archivo))
        assertFalse(temporal.exists())
        abrirConClave().use { base ->
            assertEquals("Paracetamol", nombreGuardado(base))
            assertEquals(8, base.version)
        }
    }

    @Test
    fun bdYaCifrada_quedaIgual() {
        crearSinCifrar()
        MigracionCifrado.cifrarSiHaceFalta(archivo, clave)

        //se llama cada vez que se abre la app, la segunda vez no tiene que hacer nada
        MigracionCifrado.cifrarSiHaceFalta(archivo, clave)

        abrirConClave().use { base -> assertEquals("Paracetamol", nombreGuardado(base)) }
    }

    @Test
    fun copiaIncompleta_seHaceDeNuevo() {
        crearSinCifrar()
        //simula que la app se cerro a mitad de la copia
        temporal.writeText("copia incompleta")

        MigracionCifrado.cifrarSiHaceFalta(archivo, clave)

        assertFalse(temporal.exists())
        abrirConClave().use { base -> assertEquals("Paracetamol", nombreGuardado(base)) }
    }

    @Test
    fun cierreAntesDeRenombrar_recuperaLaCopia() {
        crearSinCifrar()
        MigracionCifrado.cifrarSiHaceFalta(archivo, clave)
        //simula que la app se cerro despues de borrar la base vieja y antes de renombrar la copia
        assertTrue(archivo.renameTo(temporal))

        MigracionCifrado.cifrarSiHaceFalta(archivo, clave)

        assertFalse(temporal.exists())
        abrirConClave().use { base -> assertEquals("Paracetamol", nombreGuardado(base)) }
    }

    private fun borrarArchivos() {
        SQLiteDatabase.deleteDatabase(archivo)
        SQLiteDatabase.deleteDatabase(temporal)
    }

    //una base como la que dejaba la app antes de SQLCipher: SQLite normal de Android, version 8
    private fun crearSinCifrar() {
        archivo.parentFile?.mkdirs()
        val base = SQLiteAndroid.openOrCreateDatabase(archivo, null)
        base.execSQL("CREATE TABLE Medicamento (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL)")
        base.execSQL("INSERT INTO Medicamento (nombre) VALUES ('Paracetamol')")
        base.version = 8
        base.close()
    }

    //si la clave no es la correcta o el archivo no quedo bien, SQLCipher lanza error al abrir
    private fun abrirConClave(): SQLiteDatabase =
        SQLiteDatabase.openDatabase(archivo.path, clave, null, SQLiteDatabase.OPEN_READWRITE, null)

    private fun nombreGuardado(base: SQLiteDatabase): String =
        base.rawQuery("SELECT nombre FROM Medicamento").use {
            it.moveToFirst()
            it.getString(0)
        }
}