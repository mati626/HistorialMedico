package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface PerfilDao {
    @Query("SELECT * FROM perfiles LIMIT 1")
    fun getPerfil(): Flow<Perfil?>

    @Insert
    suspend fun insert(perfil: Perfil): Long

    @Insert
    suspend fun insertPaciente(paciente: Paciente): Long

    @Transaction
    suspend fun crearPerfil(nombre: String,registrarTitular: Boolean){
        val perfilId=insert(Perfil(nombre=nombre))
        if(registrarTitular){
            insertPaciente(Paciente(perfilId=perfilId, nombre = nombre, relacion = "Yo"))
        }
    }

}