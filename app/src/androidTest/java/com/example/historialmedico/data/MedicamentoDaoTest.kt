package com.example.historialmedico.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicamentoDaoTest {
    private lateinit var db: AppDataBase
    private var pacienteId=0L

    @Before
    fun crearBaseConPaciente(){
        val context= InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        runBlocking {
            val perfilId = db.perfilDao().insert(Perfil(nombre = "Juan Lopez"))
            pacienteId = db.pacienteDao().insert(Paciente(perfilId = perfilId, nombre = "Juan Lopez", relacion = "Yo"))
        }
    }

    @After
    fun cerrarBase(){
        db.close()
    }

    @Test
    fun insertConAlarmas_dosHoras_guardaDosAlarmas(){
        runBlocking {
            val medicamento= Medicamento(pacienteId=pacienteId, nombre = "Enalapril Maleato", dosis = "10 mg")
            val alarmas=db.medicamentoDao().insertConAlarmas(medicamento, listOf(480,1200))

            assertEquals(2, alarmas.size)
            assertTrue(alarmas.all { it.id>0 && it.activo })
            val guardadas = db.alarmaDao().getDeMedicamentos(pacienteId).first()
            assertEquals(listOf(480,1200), guardadas.map{it.hora})
        }
    }

    @Test
    fun insertConAlarmas_sinHoras_guardaSoloMedicamento(){
        runBlocking {
            val medicamento= Medicamento(pacienteId=pacienteId, nombre = "Enalapril Maleato", dosis = "10 mg")
            val alarmas=db.medicamentoDao().insertConAlarmas(medicamento, emptyList())

            assertEquals(0, alarmas.size)
            assertEquals(1, db.medicamentoDao().getByPaciente(pacienteId).first().size)
        }
    }
}