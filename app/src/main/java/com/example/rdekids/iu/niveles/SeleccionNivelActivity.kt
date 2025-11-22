package com.example.rdekids.iu.niveles

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.rdekids.iu.juego.JuegoActivity
import com.example.rdekids.R

class SeleccionNivelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_nivel)

        val puertaAntes = findViewById<ImageView>(R.id.puertaAntes)
        val puertaDurante = findViewById<ImageView>(R.id.puertaDurante)
        val puertaDespues = findViewById<ImageView>(R.id.puertaDespues)

        puertaAntes.setOnClickListener { abrirNivel("antes") }
        puertaDurante.setOnClickListener { abrirNivel("durante") }
        puertaDespues.setOnClickListener { abrirNivel("despues") }
    }

    private fun abrirNivel(fase: String) {
        val intent = Intent(this, JuegoActivity::class.java)
        intent.putExtra("faseSeleccionada", fase)
        startActivity(intent)
        finish()
    }
}