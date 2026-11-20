package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HoraMedicaDao {
    @Query("SELECT * FROM horas_medicas WHERE pacienteId = :pacienteId ORDER BY fechaHora ASC")
    fun getByPaciente(pacienteId: Long): Flow<List<HoraMedica>>

    @Query("SELECT * FROM horas_medicas WHERE id = :id")
    suspend fun getById(id: Long): HoraMedica?

    @Insert
    suspend fun insert(horaMedica: HoraMedica): Long

    @Update
    suspend fun update(horaMedica: HoraMedica)

    @Delete
    suspend fun delete(horaMedica: HoraMedica)

    @Insert
    suspend fun insertAlarma(alarma: Alarma): Long

    @Delete
    suspend fun deleteAlarma(alarma: Alarma)

    @Transaction
    suspend fun insertConRecordatorio(horaMedica: HoraMedica, recordatorioMillis: Long?): Alarma? {
        val horaMedicaId = insert(horaMedica)
        if (recordatorioMillis == null) return null
        val alarma = Alarma(horaMedicaId = horaMedicaId, fechaHoraMillis = recordatorioMillis)
        return alarma.copy(id = insertAlarma(alarma))
    }

    //cambia la hora y rehace su recordatorio, borra el anterior y crea el nuevo si hay uno
    @Transaction
    suspend fun updateConRecordatorio(
        horaMedica: HoraMedica,
        anterior: Alarma?,
        recordatorioMillis: Long?
    ): Alarma? {
        update(horaMedica)
        if (anterior != null) deleteAlarma(anterior)
        if (recordatorioMillis == null) return null
        val alarma = Alarma(horaMedicaId = horaMedica.id, fechaHoraMillis = recordatorioMillis)
        return alarma.copy(id = insertAlarma(alarma))
    }
}