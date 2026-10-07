package com.example.historialmedico.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfiles")
data class Perfil (
    @PrimaryKey(autoGenerate = true) val id: Long=0,
    val nombre: String,
    val relacion: String
)