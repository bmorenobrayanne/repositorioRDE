package com.example.rdekids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvUsuario: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIniciar = findViewById<Button>(R.id.Iniciar)
        val btnReanudar = findViewById<Button>(R.id.Reanudar)
        val btnConfiguracion = findViewById<Button>(R.id.Configuracion)
        val titulo = findViewById<TextView>(R.id.titulo)

        // Intentar encontrar el TextView (puede no existir en el layout)
        val posibleTvUsuario = findViewById<TextView?>(R.id.tvUsuario)
        if (posibleTvUsuario != null) {
            tvUsuario = posibleTvUsuario
        }

        // Animación del título
        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        titulo.startAnimation(bounce)

        // 🔹 Verificar sesión activa
        val prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        val usuario = prefs.getString("usuarioActual", null)

        if (usuario.isNullOrEmpty()) {
            // No hay sesión → redirigir a LoginActivity y cerrar MainActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Mostrar usuario actual solo si el TextView existe
        if (::tvUsuario.isInitialized) {
            tvUsuario.text = "Usuario: $usuario"
        }

        //Botón INICIAR
        btnIniciar.setOnClickListener {
            val intent = Intent(this, JuegoActivity::class.java)
            startActivity(intent)
        }

        //Botón REANUDAR
        btnReanudar.setOnClickListener {
            val intent = Intent(this, JuegoActivity::class.java)
            intent.putExtra("reanudar", true)
            startActivity(intent)
        }

        // Botón CONFIGURACIÓN
        btnConfiguracion.setOnClickListener {
            val intent = Intent(this, ConfiguracionActivity::class.java)
            startActivity(intent)
        }
    }
}




