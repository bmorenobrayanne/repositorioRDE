package com.example.rdekids.local.viewModel

import android.content.Context
import androidx.lifecycle.*
import com.example.rdekids.repository.SyncRepository
import kotlinx.coroutines.launch

class SyncViewModel(private val repo: SyncRepository) : ViewModel() {

    private val _syncCompletada = MutableLiveData<Boolean>()
    val syncCompletada: LiveData<Boolean> get() = _syncCompletada

    fun sincronizarTodo() = viewModelScope.launch {
        repo.sincronizarUsuariosPendientes()
        repo.sincronizarTodo()
        _syncCompletada.postValue(true)
    }
}