    package com.example.rdekids.local.dao

    import androidx.room.*
    import com.example.rdekids.local.entities.Puntaje

    @Dao
    interface PuntajeDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(p: Puntaje): Long

        @Query("SELECT * FROM puntaje WHERE synced = 0")
        suspend fun getPendientes(): List<Puntaje>

        @Query("UPDATE puntaje SET synced = 1 WHERE id = :id")
        suspend fun marcarSincronizado(id: Long)

        @Query("SELECT * FROM puntaje WHERE usuario = :nombreJugador")
        suspend fun obtenerPuntajesDeUsuario(nombreJugador: String): List<Puntaje>

        @Delete
        suspend fun eliminarPuntaje(puntaje: Puntaje)
    }