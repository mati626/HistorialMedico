package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {
    @Query("""
        SELECT alarmas.* FROM alarmas
        INNER JOIN medicamentos ON alarmas.medicamentoId = medicamentos.id
        WHERE medicamentos.pacienteId = :pacienteId
        ORDER BY alarmas.hora ASC
    """)
    fun getDeMedicamentos(pacienteId: Long): Flow<List<Alarma>>

    @Query("""
        SELECT alarmas.* FROM alarmas
        INNER JOIN horas_medicas ON alarmas.horaMedicaId = horas_medicas.id
        WHERE horas_medicas.pacienteId = :pacienteId
    """)
    fun getDeHorasMedicas(pacienteId: Long): Flow<List<Alarma>>

    @Query("""
        SELECT alarmas.id FROM alarmas
        LEFT JOIN medicamentos ON alarmas.medicamentoId = medicamentos.id
        LEFT JOIN horas_medicas ON alarmas.horaMedicaId = horas_medicas.id
        WHERE medicamentos.pacienteId = :pacienteId OR horas_medicas.pacienteId = :pacienteId
    """)
    suspend fun getIdsByPaciente(pacienteId: Long): List<Long>

    @Query("SELECT * FROM alarmas WHERE id = :id")
    suspend fun getById(id: Long): Alarma?

    @Query("SELECT * FROM alarmas WHERE activo = 1")
    suspend fun getActivas(): List<Alarma>

}