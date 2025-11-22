package com.example.rdekids.repository

import android.content.Context
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.local.AppDatabase
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.local.entities.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SyncRepository(
    private val db: AppDatabase,
    private val context: Context
) {

    // -------------------------------------------------
    //                  USUARIOS
    // -------------------------------------------------


    // ⭐ NUEVO: Registrar usuario offline → siempre con sincronizado = false
    suspend fun registrarUsuarioOffline(usuario: Usuario) = withContext(Dispatchers.IO) {
        val usuarioPendiente = usuario.copy(synced = false)
        db.usuarioDao().insert(usuarioPendiente)
    }

    suspend fun iniciarSesionLocal(correo: String, contrasena: String): Usuario? =
        withContext(Dispatchers.IO) {
            db.usuarioDao().iniciarSesion(correo, contrasena)
        }

    suspend fun obtenerUsuarioPorCorreoLocal(correo: String): Usuario? =
        withContext(Dispatchers.IO) {
            db.usuarioDao().obtenerUsuarioPorCorreo(correo)
        }


    // -------------------------------------------------
    //                    PUNTAJES
    // -------------------------------------------------

    suspend fun guardarPuntajeLocal(puntaje: Puntaje) = withContext(Dispatchers.IO) {
        db.puntajeDao().insert(puntaje)
    }

    suspend fun obtenerPuntajesDeUsuarioLocal(nombreJugador: String): List<Puntaje> =
        withContext(Dispatchers.IO) {
            db.puntajeDao().obtenerPuntajesDeUsuario(nombreJugador)
        }


    // -------------------------------------------------
    //                SINCRONIZACIÓN
    // -------------------------------------------------

    // ⭐ NUEVO: Solo sincroniza usuarios pendientes
    suspend fun sincronizarUsuariosPendientes() = withContext(Dispatchers.IO) {
        val usuariosPendientes = db.usuarioDao().getPendientes()

        for (u in usuariosPendientes) {

            val json = JSONObject().apply {
                put("action", "registrarUsuario")
                put("nombre", u.nombre)
                put("correo", u.correo)
                put("contrasena", u.contrasena)
                put("fechaRegistro", u.fecha)
            }

            if (GoogleSheetsService.enviarPostSimple(json)) {
                db.usuarioDao().marcarSincronizado(u.id)
            }
        }
    }

    // 🔄 YA EXISTENTE, PERO AHORA INCLUYE AUTOMÁTICAMENTE REGISTROS OFFLINE
    suspend fun sincronizarTodo() = withContext(Dispatchers.IO) {

        // 1️⃣ Usuarios
        val usuariosPend = db.usuarioDao().getPendientes()
        for (u in usuariosPend) {
            val json = JSONObject().apply {
                put("action", "registrarUsuario")
                put("nombre", u.nombre)
                put("correo", u.correo)
                put("contrasena", u.contrasena)
                put("fechaRegistro", u.fecha)
            }

            if (GoogleSheetsService.enviarPostSimple(json)) {
                db.usuarioDao().marcarSincronizado(u.id)
            }
        }

        // 2️⃣ Puntajes
        val puntajesPend = db.puntajeDao().getPendientes()
        for (p in puntajesPend) {
            val json = JSONObject().apply {
                put("action", "enviarPuntaje")
                put("usuario", p.usuario)
                put("puntaje", p.puntaje)
                put("fecha", p.fecha)
            }

            if (GoogleSheetsService.enviarPostSimple(json)) {
                db.puntajeDao().marcarSincronizado(p.id)
            }
        }
    }


    // -------------------------------------------------
    //          BORRADO BIDIRECCIONAL (sin cambios)
    // -------------------------------------------------

    suspend fun eliminarPutajeBidireccional(puntaje: Puntaje) =
        withContext(Dispatchers.IO) {

            val json = JSONObject().apply {
                put("action", "eliminarPartida")
                put("nombreJugador", puntaje.usuario)
                put("puntaje", puntaje.puntaje)
                put("fecha", puntaje.fecha)
            }

            if (GoogleSheetsService.enviarPostSimple(json)) {
                db.puntajeDao().eliminarPuntaje(puntaje)
            }
        }
}