package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HoraMedicaDao {
    @Query("SELECT * FROM horas_medicas WHERE perfilId = :perfilId ORDER BY fecha ASC")
    fun getByPerfil(perfilId: Long): Flow<List<HoraMedica>>

    @Insert
    suspend fun insert(horaMedica: HoraMedica): Long
    @Delete
    suspend fun delete(horaMedica: HoraMedica)
}

