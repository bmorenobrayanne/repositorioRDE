package com.example.rdekids.session

import android.content.Context

object SessionManager {

    private const val PREF_NAME = "SesionUsuario"
    private const val KEY_USUARIO = "usuarioActual"
    private const val KEY_LOGUEADO = "logueado"

    // Guardar usuario logueado
    fun guardarSesion(context: Context, usuario: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USUARIO, usuario)
            .putBoolean(KEY_LOGUEADO, true)
            .apply()
    }

    //Obtener usuario actual
    fun obtenerUsuario(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USUARIO, null)
    }

    //Verificar si hay una sesión activa
    fun estaLogueado(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOGUEADO, false)
    }

    //Cerrar sesión
    fun cerrarSesion(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .clear()
            .apply()
    }
}





