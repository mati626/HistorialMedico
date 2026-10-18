package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ControlTomaDao {
    @Query(""" SELECT controles_toma.* FROM controles_toma
    INNER JOIN medicamentos ON controles_toma.medicamentoId=medicamentos.id
    WHERE medicamentos.pacienteId=:pacienteId AND controles_toma.fecha=:fecha """)
    fun getDelDia(pacienteId: Long,fecha: String): Flow<List<ControlToma>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(controlToma: ControlToma)
}