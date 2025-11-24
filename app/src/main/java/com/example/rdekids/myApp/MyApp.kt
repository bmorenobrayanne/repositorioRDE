package com.example.rdekids.myApp

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.rdekids.local.AppDataBaseRoom
import com.example.rdekids.repository.SyncRepository

class MyApp : Application() {

    // Base de datos local (Room)
    val dbroom: AppDataBaseRoom by lazy {
        Room.databaseBuilder(
            this as Context,
            AppDataBaseRoom::class.java,
            "room_database_rde"
        ).build()
    }

    // Repositorio central encargado de sincronización
    val syncRepo: SyncRepository by lazy {
        SyncRepository(dbroom, this)
    }
}