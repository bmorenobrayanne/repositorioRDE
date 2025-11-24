package com.example.rdekids.tareas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rdekids.databinding.ActivityTareasBinding
import com.example.rdekids.iu.adapter.PartidasAdapter
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.local.viewModel.PuntajeViewModel
import com.example.rdekids.repository.SyncRepository
import com.example.rdekids.local.AppDataBaseRoom

class TareasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTareasBinding
    private lateinit var adapter: PartidasAdapter

    // Inicializamos el ViewModel usando lazy
    private val viewModel: PuntajeViewModel by lazy {
        val repo = SyncRepository(AppDataBaseRoom.getDataBaseRoom(this), this)
        PuntajeViewModel(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView y Adapter
        adapter = PartidasAdapter(mutableListOf())
        binding.recyclerPartidas.layoutManager = LinearLayoutManager(this)
        binding.recyclerPartidas.adapter = adapter

        // Obtener el usuario actual desde SharedPreferences
        val prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
        val usuarioActual = prefs.getString("usuarioActual", null)

        Log.d("Sesion", "Usuario leído: $usuarioActual")

        if (usuarioActual == null) {
            Toast.makeText(this, "No se encontró el usuario en sesión", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Observar LiveData del ViewModel
        viewModel.puntajesLiveData.observe(this) { lista ->
            adapter.actualizarLista(lista)
        }

        // Cargar partidas offline
        viewModel.cargarPartidasOffline(usuarioActual)
    }
}
