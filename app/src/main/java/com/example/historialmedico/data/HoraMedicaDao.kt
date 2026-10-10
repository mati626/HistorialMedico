package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface HoraMedicaDao {
    @Query("SELECT * FROM horas_medicas WHERE pacienteId = :pacienteId ORDER BY fechaHora ASC")
    fun getByPaciente(pacienteId: Long): Flow<List<HoraMedica>>

    @Query("SELECT * FROM horas_medicas WHERE id = :id")
    suspend fun getById(id: Long): HoraMedica?

    @Insert
    suspend fun insert(horaMedica: HoraMedica): Long
    @Delete
    suspend fun delete(horaMedica: HoraMedica)

    @Insert
    suspend fun insertAlarma(alarma: Alarma): Long

    @Transaction
    suspend fun insertConRecordatorio(horaMedica: HoraMedica,recordatorioMillis: Long?): Alarma?{
        val horaMedicaId=insert(horaMedica)
        if (recordatorioMillis==null)return null
        val alarma= Alarma(horaMedicaId=horaMedicaId, fechaHoraMillis = recordatorioMillis)
        return alarma.copy(id= insertAlarma(alarma))
    }
}

