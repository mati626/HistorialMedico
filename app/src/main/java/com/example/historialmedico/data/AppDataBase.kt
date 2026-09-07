package com.example.historialmedico.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Perfil::class], version = 1, exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun perfilDao(): PerfilDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase?=null

        fun getInstance(context: Context): AppDataBase=
            INSTANCE?:synchronized(this){
                INSTANCE?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "historial_medico.db"
                ).build().also { INSTANCE=it }
            }
    }
}