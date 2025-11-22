package com.example.rdekids.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rdekids.myApp.MyApp
import com.example.rdekids.repository.SyncRepository



class SyncWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            // Obtener MyApp correctamente
            val app = ctx.applicationContext as MyApp

            // Construir repo correctamente
            val repo = SyncRepository(app.db, ctx)

            // Ejecutar sincronización
            repo.sincronizarTodo()

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}