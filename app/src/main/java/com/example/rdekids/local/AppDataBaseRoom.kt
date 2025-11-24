package com.example.rdekids.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rdekids.local.dao.PuntajeDao
import com.example.rdekids.local.dao.UsuarioDao
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.local.entities.Usuario

@Database(entities = [Usuario::class, Puntaje::class], version = 1)
abstract class AppDataBaseRoom : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntajeDao(): PuntajeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBaseRoom? = null

        fun getDataBaseRoom(context: Context): AppDataBaseRoom {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBaseRoom::class.java,
                    "room_database_rde"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}