package com.example.historialmedico.data

import androidx.room.ColumnInfo
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
    val grupoSanguineo: GrupoSanguineo?=null,
    //datos para emergencias, todos opcionales
    val alergias: String? = null,
    val enfermedades: String? = null,
    val contactoNombre: String? = null,
    val contactoTelefono: String? = null,
    //el dueño decide si se ven sin PIN, parte apagado y los pacientes que ya existen quedan apagados
    @ColumnInfo(defaultValue = "0") val mostrarEmergencia: Boolean = false
)