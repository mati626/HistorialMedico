package com.example.historialmedico.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//lo que se ve sin PIN: si esto falla se mostrarian datos que el dueño no acepto mostrar
@RunWith(AndroidJUnit4::class)
class EmergenciaDaoTest {
    private lateinit var db: AppDataBase
    private var juanId = 0L
    private var rosaId = 0L

    @Before
    fun crearBaseConDosPacientes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        runBlocking {
            val perfilId = db.perfilDao().insert(Perfil(nombre = "Juan Perez"))
            juanId = db.pacienteDao().insert(
                Paciente(perfilId = perfilId, nombre = "Juan Perez", relacion = "Yo")
            )
            rosaId = db.pacienteDao().insert(
                Paciente(perfilId = perfilId, nombre = "Rosa Munoz", relacion = "Madre", alergias = "Penicilina")
            )
            db.medicamentoDao().insert(Medicamento(pacienteId = juanId, nombre = "Losartan", dosis = "50 mg"))
            db.medicamentoDao().insert(Medicamento(pacienteId = rosaId, nombre = "Enalapril", dosis = "10 mg"))
        }
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun sinActivar_noSeMuestraNadie() {
        runBlocking {
            assertFalse(db.pacienteDao().hayEmergencia().first())
            assertTrue(db.pacienteDao().getParaEmergencia().first().isEmpty())
        }
    }

    @Test
    fun activarUno_soloSaleEseConSusMedicamentos() {
        runBlocking {
            db.pacienteDao().setMostrarEmergencia(rosaId, true)

            assertTrue(db.pacienteDao().hayEmergencia().first())
            val visibles = db.pacienteDao().getParaEmergencia().first()
            assertEquals(1, visibles.size)
            assertEquals("Rosa Munoz", visibles[0].paciente.nombre)
            //activar no borra lo que ya tenia guardado
            assertEquals("Penicilina", visibles[0].paciente.alergias)
            //solo sus medicamentos, no los de Juan
            assertEquals(listOf("Enalapril"), visibles[0].medicamentos.map { it.nombre })
        }
    }

    @Test
    fun desactivar_dejaDeMostrarse() {
        runBlocking {
            db.pacienteDao().setMostrarEmergencia(rosaId, true)
            db.pacienteDao().setMostrarEmergencia(rosaId, false)

            assertFalse(db.pacienteDao().hayEmergencia().first())
            assertTrue(db.pacienteDao().getParaEmergencia().first().isEmpty())
        }
    }
}