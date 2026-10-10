package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Query("SELECT * FROM pacientes WHERE perfilId = :perfilId ORDER BY nombre ASC")
    fun getByPerfil(perfilId: Long): Flow<List<Paciente>>

    @Query("SELECT * FROM pacientes WHERE id = :id")
    suspend fun getById(id: Long): Paciente?

    @Insert
    suspend fun insert(paciente: Paciente): Long

    @Delete
    suspend fun delete(paciente: Paciente)
}