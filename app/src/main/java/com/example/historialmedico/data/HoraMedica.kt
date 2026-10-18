package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "horas_medicas",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices=[Index("pacienteId")]
)

data class HoraMedica(
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    val pacienteId: Long,
    val especialidad: String,
    val fechaHora: Long,
    val lugar: String
)