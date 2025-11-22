package com.example.rdekids.tareas

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientTareas {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    val api: ApiTareaService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiTareaService::class.java)
    }
}
