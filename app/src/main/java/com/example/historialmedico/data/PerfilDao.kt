package com.example.historialmedico.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {
    @Query("SELECT*FROM perfiles ORDER BY nombre ASC")
    fun getAll(): Flow<List<Perfil>>

    @Insert
    suspend fun insert(perfil: Perfil): Long

    @Delete
    suspend fun delete(perfil: Perfil)
}