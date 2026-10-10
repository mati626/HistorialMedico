package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "examenes",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HoraMedica::class,
            parentColumns = ["id"],
            childColumns = ["horaMedicaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("pacienteId"), Index("horaMedicaId")]
)
data class Examen(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pacienteId: Long,
    val horaMedicaId: Long?=null,
    val tipo: String,
    val fecha: String?=null,
    val resultado: String?=null,
    val estado: EstadoExamen= EstadoExamen.SOLICITADO,
    val documentoUri: String?=null
)