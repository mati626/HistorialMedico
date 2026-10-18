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
class PerfilDaoTest {
    private lateinit var db: AppDataBase

    @Before
    fun crearBaseEnMemoria(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
    }

    @After
    fun cerrarBase(){
        db.close()
    }

    @Test
    fun crearPerfil_conTitular_creaPacienteYo(){
        runBlocking {
            db.perfilDao().crearPerfil("Titular Prueba", registrarTitular = true)

            val perfil=checkNotNull(db.perfilDao().getPerfil().first())
            val pacientes=db.pacienteDao().getByPerfil(perfil.id).first()

            assertEquals(1,pacientes.size)
            assertEquals("Yo", pacientes[0].relacion)
        }
    }

    @Test
    fun crearPerfil_sinTitular_noCreaPacientes(){
        runBlocking {
            db.perfilDao().crearPerfil("Cuidador Prueba", registrarTitular = false)

            val perfil=checkNotNull(db.perfilDao().getPerfil().first())
            val pacientes=db.pacienteDao().getByPerfil(perfil.id).first()

            assertEquals(0,pacientes.size)
        }
    }
}