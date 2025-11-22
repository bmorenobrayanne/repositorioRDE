package com.example.rdekids.iu.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import com.example.rdekids.R
import com.example.rdekids.iu.registro.RegistroActivity
import com.example.rdekids.local.factory.SyncViewModelFactory
import com.example.rdekids.local.viewModel.SyncViewModel
import com.example.rdekids.myApp.MyApp
import com.example.rdekids.remote.GoogleSheetsService
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var btnRegistrar: AppCompatButton

    // ViewModel de sincronización
    private lateinit var syncViewModel: SyncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar ViewModel
        val app = application as MyApp
        val factory = SyncViewModelFactory(app.syncRepo)
        syncViewModel = ViewModelProvider(this, factory).get(SyncViewModel::class.java)

        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        // ============================================================
        //       🔥 ACCESO AUTOMÁTICO OFFLINE SI HAY SESIÓN GUARDADA
        // ============================================================
        if (!hayInternet()) {
            verificarSesionOffline()
        }

        // ============================================================
        //                   🔒 BOTÓN LOGIN (solo online)
        // ============================================================
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ❌ NO permitir login sin Internet
            if (!hayInternet()) {
                Toast.makeText(
                    this,
                    "Sin conexión. Usa el acceso automático si ya habías iniciado sesión.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // --------------------------------------------------------
            //             🟦 VALIDAR CON GOOGLE SHEETS
            // --------------------------------------------------------
            GoogleSheetsService.obtenerUsuarios { usuariosJSONArray ->
                runOnUiThread {
                    if (usuariosJSONArray == null) {
                        Toast.makeText(
                            this,
                            "Error conectando con el servidor",
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

                        if (correo.equals(correoSheet, ignoreCase = true)
                            && contrasena == contrasenaSheet
                        ) {
                            usuarioEncontrado = true

                            guardarSesion(correo, nombre)

                            // 🔄 Sincronización general
                            syncViewModel.sincronizarTodo()

                            Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            break
                        }
                    }

                    if (!usuarioEncontrado) {
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRegistrar.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    // ============================================================
    //                        🔐 SESIÓN OFFLINE
    // ============================================================
    private fun verificarSesionOffline() {
        val prefsSesion = getSharedPreferences("Sesion", MODE_PRIVATE)
        val prefsUsuarios = getSharedPreferences("Usuarios", MODE_PRIVATE)

        val correo = prefsSesion.getString("correoActual", null)
        val nombreDirecto = prefsUsuarios.getString("${correo}_nombre", null)

        if (nombreDirecto != null) {
            mostrarDialogoOffline(nombreDirecto)
            return
        }

        // Backup antiguo
        val prefsBackup = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
        val nombreBackup = prefsBackup.getString("usuarioActual", null)

        if (!nombreBackup.isNullOrEmpty()) {
            mostrarDialogoOffline(nombreBackup)
        }
    }

    private fun mostrarDialogoOffline(nombre: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Modo Offline")
            .setMessage(
                "Bienvenido nuevamente $nombre.\n" +
                        "No tienes conexión, pero tu sesión previa fue cargada correctamente.\n\n" +
                        "¿Qué deseas hacer?"
            )
            .setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton("Registrarse") { dialog, _ ->
                dialog.dismiss()
                // 👉 Se dirige a registro, donde se guarda localmente
                startActivity(Intent(this, RegistroActivity::class.java))
            }
            .setCancelable(false)
            .show()
    }

    // ============================================================
    //                💾 GUARDAR SESIÓN ONLINE
    // ============================================================
    private fun guardarSesion(correo: String, nombre: String) {
        // Guardar sesión principal
        getSharedPreferences("Sesion", MODE_PRIVATE).edit()
            .putBoolean("logueado", true)
            .putString("correoActual", correo)
            .apply()

        // Guardar nombre asociado
        val prefsUsuarios = getSharedPreferences("Usuarios", MODE_PRIVATE)
        prefsUsuarios.edit()
            .putString("${correo}_nombre", nombre)
            .apply()

        // Backup general
        getSharedPreferences("SesionUsuario", MODE_PRIVATE).edit()
            .putBoolean("logueado", true)
            .putString("usuarioActual", nombre)
            .apply()
    }

    // ============================================================
    //                   🌐 ESTADO INTERNET
    // ============================================================
    private fun hayInternet(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}