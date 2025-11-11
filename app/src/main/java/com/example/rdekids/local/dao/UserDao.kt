package com.example.rdekids.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rdekids.local.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun registrarUsuario(usuario: User)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): User?

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun login(correo: String, contrasena: String): User?
}