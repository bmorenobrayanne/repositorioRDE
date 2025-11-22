package com.example.rdekids.local.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rdekids.local.viewModel.PuntajeViewModel
import com.example.rdekids.repository.SyncRepository

class PuntajeViewModelFactory(
    private val syncRepository: SyncRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuntajeViewModel::class.java)) {
            return PuntajeViewModel(syncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}