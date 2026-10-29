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

    @Query(
        """SELECT documentoUri FROM medicamentos WHERE pacienteId = :pacienteId AND documentoUri IS NOT NULL
        UNION ALL
        SELECT documentoUri FROM examenes WHERE pacienteId = :pacienteId AND documentoUri IS NOT NULL
    """
    )
    suspend fun getDocumentos(pacienteId: Long): List<String>

    @Insert
    suspend fun insert(paciente: Paciente): Long

    @Delete
    suspend fun delete(paciente: Paciente)
}