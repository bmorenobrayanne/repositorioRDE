package com.example.rdekids.tareas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rdekids.databinding.ActivityTareasBinding
import com.example.rdekids.model.Partida
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.iu.adapter.PartidasAdapter

class TareasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTareasBinding
    private val listaPartidas = mutableListOf<Partida>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        binding.recyclerPartidas.layoutManager = LinearLayoutManager(this)

        // Obtener el usuario actual desde SharedPreferences
        val prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
        val usuarioActual = prefs.getString("usuarioActual", null)

        Log.d("Sesion", "Usuario leído: $usuarioActual")

        if (usuarioActual == null) {
            Toast.makeText(this, "No se encontró el usuario en sesión", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Cargar partidas del usuario actual
        cargarPartidas(usuarioActual)
    }

    private fun cargarPartidas(usuario: String) {
        GoogleSheetsService.obtenerUltimasPartidas(usuario) { lista ->
            if (lista == null) {
                Log.e("TAREAS", "Error obteniendo partidas")
                return@obtenerUltimasPartidas
            }

            listaPartidas.clear()
            listaPartidas.addAll(lista)

            runOnUiThread {
                binding.recyclerPartidas.adapter = PartidasAdapter(listaPartidas)
            }
        }
    }
}

