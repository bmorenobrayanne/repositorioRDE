package com.example.rdekids.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.iterator

object ApiService {

    private const val SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxP2gc8Xn9dkt3S_T9p5NoMJrgo2jEjRdp7gw5-NZ1HXFwQi_GyiDdVRz1xMOmAivey/exec"

    fun guardarJugador(context: Context, nombre: String, puntaje: Int) {
        if (hayInternet(context)) {
            try {
                val url = URL(SCRIPT_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val json = JSONObject()
                json.put("action", "insert")
                json.put("nombre", nombre)
                json.put("puntaje", puntaje)

                val out = OutputStreamWriter(conn.outputStream)
                out.write(json.toString())
                out.flush()

                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                Log.d("ApiService", "Respuesta: $response")

                conn.disconnect()

            } catch (e: Exception) {
                e.printStackTrace()
                guardarLocal(context, nombre, puntaje)
            }
        } else {
            guardarLocal(context, nombre, puntaje)
        }
    }

    private fun guardarLocal(context: Context, nombre: String, puntaje: Int) {
        val prefs = context.getSharedPreferences("offline_jugadores", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = JSONObject()
        json.put("nombre", nombre)
        json.put("puntaje", puntaje)
        editor.putString("jugador_${System.currentTimeMillis()}", json.toString())
        editor.apply()
    }

    fun sincronizarDatosPendientes(context: Context) {
        val prefs = context.getSharedPreferences("offline_jugadores", Context.MODE_PRIVATE)
        val datos = prefs.all

        for ((_, value) in datos) {
            val json = JSONObject(value.toString())
            guardarJugador(context, json.getString("nombre"), json.getInt("puntaje"))
        }

        prefs.edit().clear().apply()
    }

    private fun hayInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(network) ?: return false
        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}