package com.example.rdekids.session

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object OfflineQueueManager {

    private const val PREF_NAME = "offline_queue"
    private const val KEY_QUEUE = "pending_requests"

    fun addRequest(context: Context, request: JSONObject) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val queue = JSONArray(prefs.getString(KEY_QUEUE, "[]"))

        queue.put(request)

        prefs.edit().putString(KEY_QUEUE, queue.toString()).apply()

        // Log para ver el contenido de la cola después de agregar la solicitud
        Log.d("OfflineQueue", "Petición guardada offline -> $request")
        Log.d("OfflineQueue", "Cola ahora: $queue")
    }

    fun getQueue(context: Context): JSONArray {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return JSONArray(prefs.getString(KEY_QUEUE, "[]"))
    }

    fun clearQueue(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_QUEUE, "[]").apply()
    }

    fun agregarRegistroUsuario(context: Context, nombre: String, correo: String, contrasena: String) {
        val obj = JSONObject().apply {
            put("type", "registro")
            put("nombre", nombre)
            put("correo", correo)
            put("contrasena", contrasena)
        }

        addRequest(context, obj)
    }
}