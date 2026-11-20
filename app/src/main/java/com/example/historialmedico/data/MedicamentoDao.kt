package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE pacienteId= :pacienteId ORDER BY nombre ASC")
    fun getByPaciente(pacienteId: Long): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE id= :id")
    suspend fun getById(id: Long): Medicamento?

    @Insert
    suspend fun insert(medicamento: Medicamento): Long

    @Update
    suspend fun update(medicamento: Medicamento)

    @Delete
    suspend fun delete(medicamento: Medicamento)

    //COALESCE: si no llevaba conteo(null) empieza a contar desde lo que se repone
    @Query("UPDATE medicamentos SET cantidad = COALESCE(cantidad, 0) + :pastillas WHERE id = :id")
    suspend fun reponer(id: Long, pastillas: Int)

    @Insert
    suspend fun insertAlarma(alarma: Alarma): Long

    @Delete
    suspend fun deleteAlarmas(alarmas: List<Alarma>)

    @Transaction
    suspend fun insertConAlarmas(medicamento: Medicamento, horas: List<Int>):
            List<Alarma> {
        val medicamentoId = insert(medicamento)
        return horas.map { hora ->
            val alarma = Alarma(medicamentoId = medicamentoId, hora = hora)
            alarma.copy(id = insertAlarma(alarma))
        }
    }

    //cambia el medicamento y sus horas por diferencia, las que siguen no se tocan
    @Transaction
    suspend fun updateConAlarmas(
        medicamento: Medicamento,
        quitadas: List<Alarma>,
        nuevas: List<Int>
    ): List<Alarma> {
        update(medicamento)
        deleteAlarmas(quitadas)
        return nuevas.map { hora ->
            val alarma = Alarma(medicamentoId = medicamento.id, hora = hora)
            alarma.copy(id = insertAlarma(alarma))
        }
    }
}