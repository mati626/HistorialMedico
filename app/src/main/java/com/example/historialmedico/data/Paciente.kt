package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pacientes",
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
data class Paciente(
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    val perfilId: Long,
    val nombre: String,
    val relacion: String,
    val fechaNacimiento: String?=null,
    val sexo: Sexo?=null,
    val estadoCivil: EstadoCivil?=null,
    val prevision: Prevision?=null,
    val grupoSanguineo: GrupoSanguineo?=null
)