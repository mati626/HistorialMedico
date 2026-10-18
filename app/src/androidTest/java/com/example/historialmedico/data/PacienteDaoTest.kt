package com.example.historialmedico.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PacienteDaoTest {
    private lateinit var db: AppDataBase
    private var pacienteId=0L

    @Before
    fun crearBaseConPaciente(){
        val context= InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        runBlocking {
            val perfilId = db.perfilDao().insert(Perfil(nombre = "Titular Prueba"))
            pacienteId = db.pacienteDao().insert(Paciente(perfilId = perfilId, nombre = "Paciente Prueba", relacion = "Yo"))
        }
    }

    @After
    fun cerrarBase(){
        db.close()
    }

    @Test
    fun borrarPaciente_borraTodoLoSuyo(){
        runBlocking {
            db.medicamentoDao().insertConAlarmas(
                Medicamento(pacienteId=pacienteId, nombre = "Medicamento Prueba", dosis = "10 mg"),
                listOf(480)
            )
            db.horaMedicaDao().insertConRecordatorio(
                HoraMedica(pacienteId=pacienteId, especialidad = "Especialidad Prueba", fechaHora = 0L, lugar = "lugar Prueba"), recordatorioMillis = 0L
            )
            assertEquals(2,db.alarmaDao().getActivas().size)
            db.presionArterialDao().insert(
                PresionArterial(pacienteId=pacienteId, sistolica = 120, diastolica = 80, fechaHora = 0L)
            )
            val paciente = checkNotNull(db.pacienteDao().getById(pacienteId))
            db.pacienteDao().delete(paciente)

            assertEquals(0,db.medicamentoDao().getByPaciente(pacienteId).first().size)
            assertEquals(0,db.horaMedicaDao().getByPaciente(pacienteId).first().size)
            assertEquals(0,db.presionArterialDao().getByPaciente(pacienteId).first().size)
            assertEquals(0,db.alarmaDao().getActivas().size)
        }
    }
}