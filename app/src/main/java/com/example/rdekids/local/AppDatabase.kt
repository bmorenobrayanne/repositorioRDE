package com.example.rdekids.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rdekids.local.dao.UsuarioDao
import com.example.rdekids.local.dao.PuntajeDao
import com.example.rdekids.local.entities.Usuario
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.repository.SyncRepository
import com.example.rdekids.tareas.Tarea
import com.example.rdekids.tareas.TareaDao



@Database(
    entities = [Usuario::class, Puntaje::class, Tarea::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntajeDao(): PuntajeDao

    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rdekids_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}