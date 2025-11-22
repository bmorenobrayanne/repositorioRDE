package com.example.rdekids.local.dao

import androidx.room.*
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.local.entities.Usuario


@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario): Long

    @Query("SELECT * FROM usuario WHERE synced = 0")
    suspend fun getPendientes(): List<Usuario>

    @Query("UPDATE usuario SET synced = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: Long)

    @Query("SELECT * FROM usuario WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario?


}