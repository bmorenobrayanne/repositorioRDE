package com.example.rdekids.tareas

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.rdekids.local.AppDatabase



class TareaRepository(private val context: Context) {

    private val tareaDao = AppDatabase.getDatabase(context).tareaDao()

    suspend fun sincronizarTareas(): List<Tarea> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClientTareas.api.obtenerTareas()

            if (response.isSuccessful) {
                val tareasApi = response.body()?.take(5)?.map {
                    Tarea(
                        titulo = it.title,
                        descripcion = if (it.completed) "Completada" else "Pendiente",
                        fecha = "2025-11-08"
                    )
                } ?: emptyList()

                tareaDao.eliminarTodas()
                tareasApi.forEach { tareaDao.insertar(it) }

                tareasApi
            } else {
                tareaDao.obtenerTodas()
            }
        } catch (e: Exception) {
            tareaDao.obtenerTodas()
        }
    }

    suspend fun obtenerTareasLocales(): List<Tarea> = withContext(Dispatchers.IO) {
        tareaDao.obtenerTodas()
    }

    suspend fun agregarTareaLocal(tarea: Tarea) = withContext(Dispatchers.IO) {
        tareaDao.insertar(tarea)
    }
}


