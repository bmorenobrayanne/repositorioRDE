package com.example.rdekids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var switchSonido: Switch
    private lateinit var btnGuardar: AppCompatButton
    private lateinit var btnCerrarSesion: AppCompatButton
    private lateinit var tvUsuario: TextView
    private lateinit var imgPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        switchSonido = findViewById(R.id.Sonido)
        btnGuardar = findViewById(R.id.GuardarConfig)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        tvUsuario = findViewById(R.id.tvUsuario)
        imgPerfil = findViewById(R.id.imgPerfil)

        //Mostrar el nombre del usuario actual o "Invitado"
        val usuario = obtenerUsuarioActual()
        tvUsuario.text = if (usuario != null) "Usuario: $usuario" else "Usuario: Invitado"

        //Cargar el valor guardado del sonido
        switchSonido.isChecked = obtenerSonido()

        //Guardar configuración
        btnGuardar.setOnClickListener {
            val sonidoActivo = switchSonido.isChecked
            guardarSonido(sonidoActivo)
            Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show()
        }

        //Cerrar sesión
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    //Obtener nombre del usuario actual desde la sesión activa
    private fun obtenerUsuarioActual(): String? {
        val prefsSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE)
        val correoActual = prefsSesion.getString("correoActual", null)

        if (correoActual != null) {
            val prefsUsuarios = getSharedPreferences("Usuarios", Context.MODE_PRIVATE)
            return prefsUsuarios.getString("${correoActual}_nombre", "Invitado")
        }
        return null
    }

    //Cerrar sesión correctamente
    private fun cerrarSesion() {
        val prefs = getSharedPreferences("Sesion", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("logueado", false)
            .remove("correoActual")
            .apply()
    }

    //Guardar estado del sonido
    private fun guardarSonido(activo: Boolean) {
        val prefs = getSharedPreferences("ConfiguracionJuego", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("sonido", activo).apply()
    }

    //Obtener estado del sonido
    private fun obtenerSonido(): Boolean {
        val prefs = getSharedPreferences("ConfiguracionJuego", Context.MODE_PRIVATE)
        return prefs.getBoolean("sonido", true)
    }
}







