package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "examenes",
    foreignKeys = [
        ForeignKey(
            entity = Perfil::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("perfilId")]
)
data class Examen(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    val tipo: String,
    val fecha: String,
    val resultado: String,
    val documentoUri: String?=null
)