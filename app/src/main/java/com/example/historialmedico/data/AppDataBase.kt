package com.example.historialmedico.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    Perfil::class,
    Paciente::class,
    Medicamento::class,
    HoraMedica::class,
    Examen::class,
    Alarma::class,
    PresionArterial::class,
    ControlToma::class],
    version=7, exportSchema = true
)
abstract class AppDataBase: RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun horaMedicaDao(): HoraMedicaDao
    abstract fun examenDao(): ExamenDao
    abstract fun alarmaDao(): AlarmaDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase?=null

        fun getInstance(context: Context): AppDataBase=
            INSTANCE?:synchronized(this){
                INSTANCE?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "historial_medico.db"
                ).fallbackToDestructiveMigrationFrom(true,1,2,3,4,5,6).build().also { INSTANCE=it }
            }
    }
}