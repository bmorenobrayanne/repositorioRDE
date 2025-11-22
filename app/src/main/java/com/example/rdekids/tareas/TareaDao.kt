package com.example.rdekids.tareas

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Delete

@Dao
interface TareaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: Tarea)

    @Query("SELECT * FROM tareas ORDER BY id DESC")
    suspend fun obtenerTodas(): List<Tarea>

    @Query("DELETE FROM tareas")
    suspend fun eliminarTodas()

    @Delete
    suspend fun eliminarTarea(tarea: Tarea)
}

