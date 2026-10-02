package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "horas_medicas",
    foreignKeys = [
        ForeignKey(
            entity = Perfil::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices=[Index("perfilId")]
)

data class HoraMedica(
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    val perfilId: Long,
    val especialidad: String,
    val fecha: String,
    val lugar: String
)