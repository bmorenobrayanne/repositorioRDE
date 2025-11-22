package com.example.rdekids.myApp

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.rdekids.local.AppDatabase
import com.example.rdekids.repository.SyncRepository

class MyApp : Application() {

    // Base de datos local (Room)
    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this as Context,
            AppDatabase::class.java,
            "rde_database"
        ).build()
    }

    // Repositorio central encargado de sincronización
    val syncRepo: SyncRepository by lazy {
        SyncRepository(db, this)
    }
}