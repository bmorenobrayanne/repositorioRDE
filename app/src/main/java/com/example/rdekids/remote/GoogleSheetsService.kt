package com.example.rdekids.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import com.example.rdekids.session.OfflineQueueManager

object GoogleSheetsService {

    private const val SCRIPT_URL =
        "https://script.google.com/macros/s/AKfycbxP2gc8Xn9dkt3S_T9p5NoMJrgo2jEjRdp7gw5-NZ1HXFwQi_GyiDdVRz1xMOmAivey/exec"

    fun enviarDatos(usuario: String, puntaje: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(SCRIPT_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val json = JSONObject().apply {
                    put("action", "enviarPuntaje")
                    put("usuario", usuario)
                    put("puntaje", puntaje)
                }

                OutputStreamWriter(conn.outputStream).use {
                    it.write(json.toString())
                    it.flush()
                }

                Log.d("GoogleSheets", "Puntaje enviado: ${conn.responseCode}")
                conn.disconnect()

            } catch (e: Exception) {
                Log.e("GoogleSheetsError", "Error al enviar puntaje: ${e.message}")
            }
        }
    }

    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        callback: (Boolean, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(SCRIPT_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val json = JSONObject().apply {
                    put("action", "registrarUsuario")
                    put("nombre", nombre)
                    put("correo", correo)
                    put("contrasena", contrasena)
                }

                OutputStreamWriter(conn.outputStream).use {
                    it.write(json.toString())
                    it.flush()
                }

                val response =
                    BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

                Log.d("GoogleSheetsService", "Respuesta registrarUsuario: $response")
                conn.disconnect()

                when {
                    response.contains("existe", ignoreCase = true) ->
                        callback(false, "El usuario o correo ya están registrados.")

                    response.contains("ok", ignoreCase = true) ->
                        callback(true, "Usuario registrado correctamente.")

                    else ->
                        callback(false, "Error desconocido en el registro.")
                }

            } catch (e: Exception) {
                Log.e("GoogleSheetsError", "Error al registrar usuario: ${e.message}")
                callback(false, "Error de conexión con el servidor.")
            }
        }
    }

    fun obtenerUsuarios(callback: (JSONArray?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(SCRIPT_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val result =
                    BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

                callback(JSONArray(result))

            } catch (e: Exception) {
                Log.e("GoogleSheetsError", "Error al obtener usuarios: ${e.message}")
                callback(null)
            }
        }
    }

    fun reintentarEnvio(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val queue = OfflineQueueManager.getQueue(context)

                for (i in 0 until queue.length()) {
                    val req = queue.getJSONObject(i)

                    when (req.getString("type")) {

                        "puntaje" -> {
                            val json = JSONObject().apply {
                                put("action", "enviarPuntaje")
                                put("usuario", req.getString("usuario"))
                                put("puntaje", req.getInt("puntaje"))
                            }
                            enviarPostSimple(json)
                        }

                        "registro" -> {
                            val json = JSONObject().apply {
                                put("action", "registrarUsuario")
                                put("nombre", req.getString("nombre"))
                                put("correo", req.getString("correo"))
                                put("contrasena", req.getString("contrasena"))
                            }
                            enviarPostSimple(json)
                        }
                    }
                }

                OfflineQueueManager.clearQueue(context)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

     fun enviarPostSimple(json: JSONObject): Boolean {
        return try {
            val url = URL(SCRIPT_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            OutputStreamWriter(conn.outputStream).use {
                it.write(json.toString())
                it.flush()
            }

            val code = conn.responseCode
            conn.disconnect()

            code == 200
        } catch (e: Exception) {
            false
        }
    }



}