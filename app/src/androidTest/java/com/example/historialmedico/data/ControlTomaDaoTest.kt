package com.example.historialmedico.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlTomaDaoTest {
    private lateinit var db: AppDataBase
    private var medicamentoId = 0L

    @Before
    fun crearBaseConMedicamento() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        runBlocking {
            val perfilId = db.perfilDao().insert(Perfil(nombre = "Juan Lopez"))
            val pacienteId = db.pacienteDao().insert(
                Paciente(perfilId = perfilId, nombre = "Juan Lopez", relacion = "Yo")
            )
            medicamentoId = db.medicamentoDao().insert(
                Medicamento(pacienteId = pacienteId, nombre = "Enalapril Maleato", dosis = "10 mg", cantidad = 10)
            )
        }
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun marcarToma_dobleToque_descuentaUnaVez() {
        runBlocking {
            //dos toques seguidos sobre la misma toma de las 08:00
            db.controlTomaDao().marcarToma(medicamentoId, "2026-11-08", 480, true)
            db.controlTomaDao().marcarToma(medicamentoId, "2026-11-08", 480, true)
            assertEquals(9, db.medicamentoDao().getById(medicamentoId)?.cantidad)

            //desmarcar devuelve la pastilla
            db.controlTomaDao().marcarToma(medicamentoId, "2026-11-08", 480, false)
            assertEquals(10, db.medicamentoDao().getById(medicamentoId)?.cantidad)
        }
    }
}