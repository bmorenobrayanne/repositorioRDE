package com.example.rdekids.local.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rdekids.local.viewModel.SyncViewModel
import com.example.rdekids.repository.SyncRepository

class SyncViewModelFactory(private val repo: SyncRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyncViewModel(repo) as T
    }
}