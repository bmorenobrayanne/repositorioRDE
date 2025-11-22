package com.example.rdekids.local.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rdekids.local.viewModel.UsuarioViewModel
import com.example.rdekids.repository.SyncRepository


class UsuarioViewModelFactory(
    private val repo: SyncRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UsuarioViewModel(repo, appContext) as T
    }
}