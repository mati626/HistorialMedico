package com.example.historialmedico.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExamenDaoTest {
    private lateinit var db: AppDataBase
    private var pacienteId = 0L

    @Before
    fun crearBaseConPaciente() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        runBlocking {
            val perfilId = db.perfilDao().insert(Perfil(nombre = "Juan Lopez"))
            pacienteId = db.pacienteDao().insert(
                Paciente(perfilId = perfilId, nombre = "Juan Lopez", relacion = "Yo")
            )
        }
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun borrarHoraMedica_examenQuedaSinVinculo() {
        runBlocking {
            val horaMedica = HoraMedica(
                pacienteId = pacienteId,
                especialidad = "Nefrologia",
                fechaHora = 1_795_000_000_000L,
                lugar = "Hospital Regional"
            )
            val horaMedicaId = db.horaMedicaDao().insert(horaMedica)
            db.examenDao().insert(
                Examen(pacienteId = pacienteId, horaMedicaId = horaMedicaId, tipo = "Hemograma")
            )

            //borrar la hora no debe borrar el examen, solo soltar el vinculo
            db.horaMedicaDao().delete(horaMedica.copy(id = horaMedicaId))

            val examenes = db.examenDao().getByPaciente(pacienteId).first()
            assertEquals(1, examenes.size)
            assertNull(examenes[0].horaMedicaId)
        }
    }
}