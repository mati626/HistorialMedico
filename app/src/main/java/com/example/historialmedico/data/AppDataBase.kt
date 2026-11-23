package com.example.historialmedico.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.historialmedico.security.ClaveBaseDatos
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        Perfil::class,
        Paciente::class,
        Medicamento::class,
        HoraMedica::class,
        Examen::class,
        Alarma::class,
        PresionArterial::class,
        ControlToma::class],
    version = 9, exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        //columnas de emergencia en pacientes
        AutoMigration(from = 8, to = 9)
    ]
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun horaMedicaDao(): HoraMedicaDao
    abstract fun examenDao(): ExamenDao
    abstract fun alarmaDao(): AlarmaDao

    abstract fun presionArterialDao(): PresionArterialDao

    abstract fun controlTomaDao(): ControlTomaDao

    companion object {
        private const val NOMBRE = "historial_medico.db"

        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: crear(context.applicationContext).also { INSTANCE = it }
            }

        //la base queda cifrada con SQLCipher, los DAO se usan igual que antes
        private fun crear(context: Context): AppDataBase {
            //SQLCipher trae su propio SQLite y no se carga solo, hay que cargarlo antes de abrir la base
            System.loadLibrary("sqlcipher")
            val clave = ClaveBaseDatos.obtener(context)
            //tiene que ir antes de que Room abra el archivo, Room no puede abrir con clave una base sin cifrar
            MigracionCifrado.cifrarSiHaceFalta(context.getDatabasePath(NOMBRE), clave)
            return Room.databaseBuilder(context, AppDataBase::class.java, NOMBRE)
                .openHelperFactory(SupportOpenHelperFactory(clave))
                .fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5, 6)
                .build()
        }
    }
}