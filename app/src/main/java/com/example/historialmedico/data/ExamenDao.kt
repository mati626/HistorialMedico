package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamenDao {
    @Query("SELECT * FROM examenes WHERE pacienteId=:pacienteId ORDER BY fecha ASC")
    fun getByPaciente(pacienteId: Long): Flow<List<Examen>>

    @Insert
    suspend fun insert(examen:Examen): Long
    @Delete
    suspend fun delete(examen: Examen)
}