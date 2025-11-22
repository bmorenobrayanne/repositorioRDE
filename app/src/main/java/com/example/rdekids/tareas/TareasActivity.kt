package com.example.rdekids.tareas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rdekids.R
import com.example.rdekids.iu.login.MainActivity
import org.json.JSONArray
import org.json.JSONObject

class TareasActivity : AppCompatActivity() {

    private lateinit var tituloInput: EditText
    private lateinit var descripcionInput: EditText
    private lateinit var fechaInput: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminarTodo: Button
    private lateinit var listaTareas: LinearLayout

    private var usuarioActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas)

        tituloInput = findViewById(R.id.tituloInput)
        descripcionInput = findViewById(R.id.descripcionInput)
        fechaInput = findViewById(R.id.fechaInput)
        btnGuardar = findViewById(R.id.btnGuardar)
        listaTareas = findViewById(R.id.listaTareas)
        btnEliminarTodo = findViewById(R.id.btnEliminarTodo)

        // Obtener el usuario actual desde la sesión
        val sesionPrefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
        usuarioActual = sesionPrefs.getString("usuarioActual", null)

        // Si no hay usuario, volver al inicio
        if (usuarioActual.isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        mostrarTareas()

        btnGuardar.setOnClickListener {
            val titulo = tituloInput.text.toString().trim()
            val descripcion = descripcionInput.text.toString().trim()
            val fecha = fechaInput.text.toString().trim()

            if (titulo.isEmpty() || descripcion.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarTarea(titulo, descripcion, fecha)
            limpiarCampos()
            mostrarTareas()
        }

        btnEliminarTodo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar todas las tareas")
                .setMessage("¿Seguro que quieres eliminar todas las tareas?")
                .setPositiveButton("Sí") { _, _ ->
                    val prefs = getSharedPreferences("Tareas_${usuarioActual}", MODE_PRIVATE)
                    prefs.edit().putString("lista_tareas", "[]").apply()
                    mostrarTareas()
                    Toast.makeText(this, "Todas las tareas eliminadas", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun guardarTarea(titulo: String, descripcion: String, fecha: String) {
        val prefs = getSharedPreferences("Tareas_${usuarioActual}", MODE_PRIVATE)
        val tareasStr = prefs.getString("lista_tareas", "[]")
        val tareasArray = JSONArray(tareasStr)

        val nuevaTarea = JSONObject()
        nuevaTarea.put("titulo", titulo)
        nuevaTarea.put("descripcion", descripcion)
        nuevaTarea.put("fecha", fecha)

        tareasArray.put(nuevaTarea)
        prefs.edit().putString("lista_tareas", tareasArray.toString()).apply()
        Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarTareas() {
        listaTareas.removeAllViews()
        val prefs = getSharedPreferences("Tareas_${usuarioActual}", MODE_PRIVATE)
        val tareasStr = prefs.getString("lista_tareas", "[]")
        val tareasArray = JSONArray(tareasStr)

        if (tareasArray.length() == 0) {
            val vacio = TextView(this)
            vacio.text = "No hay tareas registradas"
            vacio.setTextColor(resources.getColor(android.R.color.white))
            vacio.textSize = 16f
            listaTareas.addView(vacio)
            return
        }

        for (i in 0 until tareasArray.length()) {
            val tareaObj = tareasArray.getJSONObject(i)
            val view = LayoutInflater.from(this).inflate(R.layout.item_tarea, listaTareas, false)

            val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
            val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcion)
            val tvFecha = view.findViewById<TextView>(R.id.tvFecha)

            tvTitulo.text = tareaObj.getString("titulo")
            tvDescripcion.text = tareaObj.getString("descripcion")
            tvFecha.text = " " + tareaObj.getString("fecha")

            view.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Eliminar \"${tareaObj.getString("titulo")}\"?")
                    .setPositiveButton("Sí") { _, _ ->
                        eliminarTarea(i)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            listaTareas.addView(view)
        }
    }

    private fun eliminarTarea(indice: Int) {
        val prefs = getSharedPreferences("Tareas_${usuarioActual}", MODE_PRIVATE)
        val tareasStr = prefs.getString("lista_tareas", "[]")
        val tareasArray = JSONArray(tareasStr)

        val nuevaLista = JSONArray()
        for (i in 0 until tareasArray.length()) {
            if (i != indice) nuevaLista.put(tareasArray.getJSONObject(i))
        }

        prefs.edit().putString("lista_tareas", nuevaLista.toString()).apply()
        mostrarTareas()
        Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
    }

    private fun limpiarCampos() {
        tituloInput.text.clear()
        descripcionInput.text.clear()
        fechaInput.text.clear()
    }
}

