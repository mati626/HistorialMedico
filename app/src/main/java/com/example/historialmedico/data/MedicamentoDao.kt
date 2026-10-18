package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE pacienteId= :pacienteId ORDER BY nombre ASC")
    fun getByPaciente(pacienteId: Long): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE id= :id")
    suspend fun getById(id: Long): Medicamento?

    @Insert
    suspend fun insert(medicamento: Medicamento): Long

    @Delete
    suspend fun delete(medicamento: Medicamento)

    @Insert
    suspend fun insertAlarma(alarma: Alarma): Long

    @Transaction
    suspend fun insertConAlarmas(medicamento: Medicamento,horas: List<Int>):
            List<Alarma>{
        val medicamentoId=insert(medicamento)
        return horas.map { hora->
            val alarma= Alarma(medicamentoId=medicamentoId,hora=hora)
            alarma.copy(id=insertAlarma(alarma))
        }
    }
}