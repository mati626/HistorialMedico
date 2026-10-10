package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarmas",
    foreignKeys = [
        ForeignKey(
            entity = Medicamento::class,
            parentColumns = ["id"],
            childColumns = ["medicamentoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HoraMedica::class,
            parentColumns = ["id"],
            childColumns = ["horaMedicaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicamentoId"), Index("horaMedicaId")]
)
data class Alarma(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicamentoId: Long? = null,
    val horaMedicaId: Long? = null,
    val hora: Int? = null,
    val fechaHoraMillis: Long? = null,
    val activo: Boolean = true
){
    init {
        require((medicamentoId==null)!=(horaMedicaId==null)){
            "Una alarma pertenece a un medicamento o a una hora medica, nunca a ambos"
        }
        if(medicamentoId!=null){
            require(hora!=null&&hora in 0..1439 && fechaHoraMillis==null){
                "Una alarma de medicamento necesita hora (0 a 1439) y no lleva fechaHoraMillis"
            }
        }else{
            require(fechaHoraMillis!=null&&hora==null){
                "Una alarma de hora medica necesita fechaHoraMillis y no lleva hora"
            }
        }
    }
}