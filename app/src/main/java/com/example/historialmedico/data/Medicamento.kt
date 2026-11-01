package com.example.historialmedico.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicamentos",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pacienteId")]
)
data class Medicamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pacienteId: Long,
    val nombre: String,
    val dosis: String,
    val documentoUri: String? = null,
    //pastillas que quedan, null si no se lleva la cuenta
    val cantidad: Int? = null,
    @ColumnInfo(defaultValue = "1") val unidadesPorToma: Int = 1
)