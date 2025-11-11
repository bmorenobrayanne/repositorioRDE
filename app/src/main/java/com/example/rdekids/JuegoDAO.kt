package com.example.rdekids.data

import android.content.Context

class JuegoDAO(private val context: Context) {

    private val prefs = context.getSharedPreferences("partida", Context.MODE_PRIVATE)

    fun guardarPuntaje(puntaje: Int) {
        prefs.edit().putInt("puntaje", puntaje).apply()
    }

    fun cargarPuntaje(): Int {
        return prefs.getInt("puntaje", 0)
    }

    fun guardarPartida() {
        prefs.edit().apply {
            putBoolean("partida_guardada", true)
            apply()
        }
    }

    fun hayPartidaGuardada(): Boolean {
        return prefs.getBoolean("partida_guardada", false)
    }
}

