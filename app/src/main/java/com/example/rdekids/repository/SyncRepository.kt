package com.example.rdekids.repository


import android.content.Context
import android.util.Log
import com.example.rdekids.local.AppDataBaseRoom
import com.example.rdekids.local.dao.PuntajeDao
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.local.entities.Usuario
import com.example.rdekids.utils.NetworkUtils.hayInternet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SyncRepository(
    private val dbroom: AppDataBaseRoom,
    private val context: Context
) {

    // -------------------------------------------------
    //                  USUARIOS
    // -------------------------------------------------

    // Registrar usuario offline → siempre con synced = false
    suspend fun registrarUsuarioOffline(usuario: Usuario) = withContext(Dispatchers.IO) {
        val usuarioPendiente = usuario.copy(synced = false)
        dbroom.usuarioDao().insert(usuarioPendiente)
    }


    suspend fun obtenerUsuarioPorCorreoLocal(correo: String): Usuario? =
        withContext(Dispatchers.IO) {
            dbroom.usuarioDao().obtenerUsuarioPorCorreo(correo)
        }


    // -------------------------------------------------
    //                    PUNTAJES
    // -------------------------------------------------

    suspend fun guardarPuntajeLocal(puntaje: Puntaje) = withContext(Dispatchers.IO) {
        val p = puntaje.copy(synced = false)
        dbroom.puntajeDao().insert(p)
        Log.d("ROOM_PUNTAJE", "Puntaje almacenado localmente → ID = ${p.id}, Usuario = ${p.usuario}, Puntaje = ${p.puntaje}, Fecha = ${p.fecha}")
    }

    suspend fun obtenerPuntajesDeUsuarioLocal(nombreJugador: String): List<Puntaje> =
        withContext(Dispatchers.IO) {
            dbroom.puntajeDao().obtenerPuntajesDeUsuario(nombreJugador)
        }


    // -------------------------------------------------
    //                SINCRONIZACIÓN
    // -------------------------------------------------

    suspend fun sincronizarUsuariosPendientes() = withContext(Dispatchers.IO) {
        val usuariosPendientes = dbroom.usuarioDao().getPendientes()

        for (u in usuariosPendientes) {

            val json = JSONObject().apply {
                put("action", "registrarUsuario")
                put("nombre", u.nombre)
                put("correo", u.correo)
                put("contrasena", u.contrasena)
                put("fechaRegistro", u.fecha)
            }

            if (GoogleSheetsService.enviarPostSimple(json)) {
                dbroom.usuarioDao().marcarSincronizado(u.id)
            }
        }
    }

    suspend fun sincronizarTodo(context: Context) = withContext(Dispatchers.IO) {

        // 🔍 1. Verificar internet antes de sincronizar
        if (!hayInternet(context)) {
            Log.w("SYNC", "No hay internet. No se sincroniza nada.")
            return@withContext
        }

        Log.d("SYNC", "Iniciando sincronización...")

        // 1️⃣ Usuarios pendientes
        val usuariosPend = dbroom.usuarioDao().getPendientes()
        Log.d("SYNC", "Usuarios pendientes por sincronizar: ${usuariosPend.size}")

        for (u in usuariosPend) {

            val json = JSONObject().apply {
                put("action", "registrarUsuario")
                put("nombre", u.nombre)
                put("correo", u.correo)
                put("contrasena", u.contrasena)
                put("fechaRegistro", u.fecha)
            }

            val enviado = GoogleSheetsService.enviarPostSimple(json)

            if (enviado) {
                dbroom.usuarioDao().marcarSincronizado(u.id)
                Log.d("SYNC", "✔ Usuario sincronizado: ${u.nombre}")
            } else {
                Log.e("SYNC", "❌ Error al sincronizar usuario: ${u.nombre}")
                return@withContext
            }
        }

        // 2️⃣ Puntajes pendientes
        val puntajesPend = dbroom.puntajeDao().getPendientes()
        Log.d("SYNC", "Puntajes pendientes por sincronizar: ${puntajesPend.size}")

        for (p in puntajesPend) {

            val json = JSONObject().apply {
                put("action", "enviarPuntaje")
                put("usuario", p.usuario)
                put("puntaje", p.puntaje)
                put("fecha", p.fecha)
            }

            val enviado = GoogleSheetsService.enviarPostSimple(json)

            if (enviado) {
                dbroom.puntajeDao().marcarSincronizado(p.id)
                Log.d("SYNC", "✔ Puntaje sincronizado: usuario=${p.usuario}, puntaje=${p.puntaje}")
            } else {
                Log.e("SYNC", "❌ Error al sincronizar puntaje de ${p.usuario}")
                return@withContext
            }
        }

        Log.d("SYNC", "✔ Sincronización completada sin errores.")
    }


    // -------------------------------------------------
    //          ELIMINAR PARTIDA
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
                dbroom.puntajeDao().eliminarPuntaje(puntaje)
            }
        }

    suspend fun obtenerPuntajesOffline(nombreJugador: String? = null): List<Puntaje> =
        withContext(Dispatchers.IO) {
            if (nombreJugador != null) {
                dbroom.puntajeDao().obtenerPuntajesDeUsuario(nombreJugador)
            } else {
                dbroom.puntajeDao().obtenerTodosLosPuntajes()
            }
        }

}