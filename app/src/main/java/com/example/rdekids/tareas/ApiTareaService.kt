package com.example.rdekids.tareas

import retrofit2.Response
import retrofit2.http.GET

interface ApiTareaService {
    @GET("https://jsonplaceholder.typicode.com/todos")
    suspend fun obtenerTareas(): Response<List<TareaDTO>>
}

