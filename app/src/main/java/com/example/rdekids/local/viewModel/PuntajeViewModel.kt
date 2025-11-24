package com.example.rdekids.local.viewModel


import android.content.Context
import androidx.lifecycle.*
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.repository.SyncRepository
import kotlinx.coroutines.launch

class PuntajeViewModel(private val repo: SyncRepository) : ViewModel() {

    val puntajesLiveData = MutableLiveData<List<Puntaje>>()

    // Función para cargar puntajes offline
    fun cargarPartidasOffline(usuario: String) {
        viewModelScope.launch {
            val lista = repo.obtenerPuntajesOffline(usuario)
            puntajesLiveData.value = lista
        }
    }

    private val _puntajesUsuario = MutableLiveData<List<Puntaje>>()
    val puntajesUsuario: LiveData<List<Puntaje>> get() = _puntajesUsuario

    private val _eliminadoExitoso = MutableLiveData<Boolean>()
    val eliminadoExitoso: LiveData<Boolean> get() = _eliminadoExitoso

    fun guardarPuntaje(puntaje: Puntaje) = viewModelScope.launch {
        repo.guardarPuntajeLocal(puntaje)
    }

    fun obtenerPuntajes(nombreJugador: String) = viewModelScope.launch {
        val lista = repo.obtenerPuntajesDeUsuarioLocal(nombreJugador)
        _puntajesUsuario.postValue(lista)
    }

    fun eliminarPuntajeBidireccional(puntaje: Puntaje) = viewModelScope.launch {
        repo.eliminarPutajeBidireccional(puntaje)
        _eliminadoExitoso.postValue(true)
    }
}