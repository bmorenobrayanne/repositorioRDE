package com.example.rdekids.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puntaje")
data class Puntaje(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuario: String,
    val puntaje: Int,
    val fecha: Long = System.currentTimeMillis(),
    var synced: Boolean = false
)
