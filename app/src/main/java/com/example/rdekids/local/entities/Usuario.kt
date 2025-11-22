package com.example.rdekids.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val fecha: Long = System.currentTimeMillis(),
    var synced: Boolean = false
)