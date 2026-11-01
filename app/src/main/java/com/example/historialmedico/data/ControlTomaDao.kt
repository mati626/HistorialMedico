package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface ControlTomaDao {
    @Query(
        """ SELECT controles_toma.* FROM controles_toma
    INNER JOIN medicamentos ON controles_toma.medicamentoId=medicamentos.id
    WHERE medicamentos.pacienteId=:pacienteId AND controles_toma.fecha=:fecha """
    )
    fun getDelDia(pacienteId: Long, fecha: String): Flow<List<ControlToma>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(controlToma: ControlToma)

    @Query("SELECT tomado FROM controles_toma WHERE medicamentoId = :medicamentoId AND fecha = :fecha AND hora = :hora")
    suspend fun estaTomada(medicamentoId: Long, fecha: String, hora: Int): Boolean?

    @Query("UPDATE medicamentos SET cantidad = MAX(cantidad - unidadesPorToma, 0) WHERE id = :medicamentoId AND cantidad IS NOT NULL")
    suspend fun descontarPastillas(medicamentoId: Long)

    @Query("UPDATE medicamentos SET cantidad = cantidad + unidadesPorToma WHERE id = :medicamentoId AND cantidad IS NOT NULL")
    suspend fun devolverPastillas(medicamentoId: Long)

    //solo mueve la cantidad si la toma cambia de estado, asi un doble toque no descuenta 2 veces
    @Transaction
    suspend fun marcarToma(medicamentoId: Long, fecha: String, hora: Int, tomado: Boolean) {
        val anterior = estaTomada(medicamentoId, fecha, hora) ?: false
        if (anterior == tomado) return
        guardar(
            ControlToma(
                medicamentoId = medicamentoId,
                fecha = fecha,
                hora = hora,
                tomado = tomado
            )
        )
        if (tomado) descontarPastillas(medicamentoId) else devolverPastillas(medicamentoId)
    }
}