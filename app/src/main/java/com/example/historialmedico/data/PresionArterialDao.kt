package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresionArterialDao {
    @Query("SELECT * FROM presiones_arteriales WHERE pacienteId = :pacienteId ORDER BY fechaHora DESC")
    fun getByPaciente(pacienteId: Long): Flow<List<PresionArterial>>

    @Insert
    suspend fun insert(presionArterial: PresionArterial): Long

    @Delete
    suspend fun delete(presionArterial: PresionArterial)
}