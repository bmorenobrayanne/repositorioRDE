package com.example.rdekids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.rdekids.data.GoogleSheetsService

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var btnRegistrar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        //Botón de iniciar sesión
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Llamar a Google Sheets para validar usuario
            GoogleSheetsService.obtenerUsuarios { usuariosJSONArray ->
                runOnUiThread {
                    if (usuariosJSONArray == null) {
                        Toast.makeText(
                            this,
                            "Error de conexión con el servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@runOnUiThread
                    }

                    var usuarioEncontrado = false
                    for (i in 0 until usuariosJSONArray.length()) {
                        val usuarioObj = usuariosJSONArray.getJSONObject(i)
                        val correoSheet = usuarioObj.optString("correo")
                        val contrasenaSheet = usuarioObj.optString("contrasena")
                        val nombre = usuarioObj.optString("nombre")

                        if (correo.equals(correoSheet, ignoreCase = true) && contrasena == contrasenaSheet) {
                            usuarioEncontrado = true
                            guardarSesion(nombre)
                            Toast.makeText(
                                this,
                                "Bienvenido $nombre",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            break
                        }
                    }

                    if (!usuarioEncontrado) {
                        Toast.makeText(
                            this,
                            "Correo o contraseña incorrectos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        //Botón de registrar
        btnRegistrar.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun guardarSesion(nombre: String) {
        val prefs = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("logueado", true)
            .putString("usuarioActual", nombre)
            .apply()
    }
}








