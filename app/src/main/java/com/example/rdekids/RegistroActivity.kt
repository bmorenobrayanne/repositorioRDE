package com.example.rdekids

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rdekids.data.GoogleSheetsService

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Verificar si el usuario o correo ya están registrados
            verificarYRegistrarUsuario(nombre, correo, contrasena)
        }
    }

    private fun verificarYRegistrarUsuario(nombre: String, correo: String, contrasena: String) {
        Toast.makeText(this, "Verificando usuario...", Toast.LENGTH_SHORT).show()

        GoogleSheetsService.obtenerUsuarios { usuariosArray ->
            runOnUiThread {
                if (usuariosArray == null) {
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                var existe = false
                for (i in 0 until usuariosArray.length()) {
                    val user = usuariosArray.getJSONObject(i)
                    val nombreExistente = user.optString("nombre", "")
                    val correoExistente = user.optString("correo", "")

                    if (nombre.equals(nombreExistente, ignoreCase = true) ||
                        correo.equals(correoExistente, ignoreCase = true)) {
                        existe = true
                        break
                    }
                }

                if (existe) {
                    Toast.makeText(this, "El usuario o correo ya están registrados.", Toast.LENGTH_LONG).show()
                } else {
                    registrarUsuario(nombre, correo, contrasena)
                }
            }
        }
    }

    private fun registrarUsuario(nombre: String, correo: String, contrasena: String) {
        Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show()

        //Registrar usuario incluyendo la contraseña
        GoogleSheetsService.registrarUsuario(nombre, correo, contrasena) { exito, mensaje ->
            runOnUiThread {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

                if (exito) {
                    //Si el registro exitoso redirigir al login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}









