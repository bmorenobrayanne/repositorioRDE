package com.example.rdekids.local.viewModel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.rdekids.local.entities.Usuario
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.repository.SyncRepository

class UsuarioViewModel(
    private val repo: SyncRepository,
    private val appContext: Context
) : ViewModel() {

    private val _resultadoLogin = MutableLiveData<Usuario?>()
    val resultadoLogin: LiveData<Usuario?> get() = _resultadoLogin

    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> get() = _registroExitoso

    private val _errorRegistro = MutableLiveData<String?>()
    val errorRegistro: LiveData<String?> get() = _errorRegistro

    fun registrarUsuario(usuario: Usuario) = viewModelScope.launch {
        try {
            if (!hayInternet()) {
                // 🔴 SIN INTERNET → Guardar solo local
                repo.registrarUsuarioOffline(usuario)
                _registroExitoso.postValue(true)
                return@launch
            }

            // 🟢 CON INTERNET → Intentar subir a Google Sheets
            GoogleSheetsService.registrarUsuario(
                usuario.nombre,
                usuario.correo,
                usuario.contrasena
            ) { exito, mensaje ->

                if (exito) {
                    // Guardar como sincronizado
                    viewModelScope.launch {
                        repo.registrarUsuarioOffline(
                            usuario.copy(synced = true)
                        )
                    }
                    _registroExitoso.postValue(true)

                } else {
                    // Si falla: guardar offline para sincronizar luego
                    viewModelScope.launch {
                        repo.registrarUsuarioOffline(usuario)
                    }
                    _errorRegistro.postValue(mensaje)
                }
            }

        } catch (e: Exception) {
            _errorRegistro.postValue("Error: ${e.message}")
        }
    }

    private fun hayInternet(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun sincronizar() = viewModelScope.launch {
        try {
            repo.sincronizarUsuariosPendientes()
        } catch (e: Exception) {
            _errorRegistro.postValue("Error al sincronizar: ${e.message}")
        }
    }

    fun errorRegistro() {
        _errorRegistro.postValue(null)
    }
}