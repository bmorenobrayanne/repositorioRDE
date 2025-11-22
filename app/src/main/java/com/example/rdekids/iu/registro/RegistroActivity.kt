package com.example.rdekids.iu.registro


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.rdekids.databinding.ActivityRegistroBinding
import com.example.rdekids.iu.login.LoginActivity
import com.example.rdekids.local.entities.Usuario
import com.example.rdekids.local.factory.UsuarioViewModelFactory
import com.example.rdekids.local.viewModel.UsuarioViewModel
import com.example.rdekids.myApp.MyApp

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: UsuarioViewModel by viewModels {
        UsuarioViewModelFactory((application as MyApp).syncRepo,
        applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------- BOTÓN REGISTRAR ----------------
        binding.btnRegistrar.setOnClickListener {

            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            // Validación básica
            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuario = Usuario(
                nombre = nombre,
                correo = correo,
                contrasena = contrasena
            )

            viewModel.registrarUsuario(usuario)

            // Intento inmediato de sincronización si hay internet
            viewModel.sincronizar()
        }

        // ---------------- OBSERVADORES ----------------
        viewModel.registroExitoso.observe(this) { exito ->
            if (exito) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        viewModel.errorRegistro.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}