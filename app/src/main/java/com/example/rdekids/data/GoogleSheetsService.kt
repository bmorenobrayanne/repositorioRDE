package com.example.rdekids.data

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

object GoogleSheetsService {

    private const val SCRIPT_URL =
        "https://script.google.com/macros/s/AKfycbxP2gc8Xn9dkt3S_T9p5NoMJrgo2jEjRdp7gw5-NZ1HXFwQi_GyiDdVRz1xMOmAivey/exec"

    //Enviar datos del puntaje del jugador
    fun enviarDatos(usuario: String, puntaje: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$SCRIPT_URL?action=guardarPuntaje")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val data = """{"usuario":"$usuario","puntaje":$puntaje}"""
                OutputStreamWriter(conn.outputStream).use { it.write(data); it.flush() }

                val responseCode = conn.responseCode
                Log.d("GoogleSheets", "Puntaje enviado con código $responseCode")
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("GoogleSheetsError", "Error al enviar puntaje: ${e.message}")
            }
        }
    }

    //Registrar un nuevo usuario
    fun registrarUsuario(nombre: String, correo: String, contrasena: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$SCRIPT_URL?action=registrarUsuario")
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

                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()); it.flush() }

                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
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

    //Obtener lista completa de usuarios (para validar login o duplicados)
    fun obtenerUsuarios(callback: (JSONArray?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$SCRIPT_URL?action=obtenerUsuarios")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val result = reader.readText()
                reader.close()

                val jsonArray = JSONArray(result)
                callback(jsonArray)
            } catch (e: Exception) {
                Log.e("GoogleSheetsError", "Error al obtener usuarios: ${e.message}")
                callback(null)
            }
        }
    }
}

