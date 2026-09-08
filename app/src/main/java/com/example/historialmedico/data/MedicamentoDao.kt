package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE perfilId=:perfilId ORDER BY nombre ASC")
    fun getByPerfil(perfilId: Long): Flow<List<Medicamento>>

    @Insert
    suspend fun insert(medicamento: Medicamento): Long

    @Delete
    suspend fun delete(medicamento: Medicamento)
}