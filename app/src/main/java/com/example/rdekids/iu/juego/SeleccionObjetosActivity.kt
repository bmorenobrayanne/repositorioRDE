package com.example.rdekids.iu.juego

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.rdekids.R


class SeleccionObjetosActivity : AppCompatActivity() {

    private lateinit var tvPuntaje: TextView
    private lateinit var tvVidas: TextView

    private lateinit var confettiView: LottieAnimationView
    private lateinit var btnConfirmar: Button

    // Ahora son ImageView (layout que me pasaste usa ImageView)
    private lateinit var ivBotiquin: ImageView
    private lateinit var ivLinterna: ImageView
    private lateinit var ivPelota: ImageView
    private lateinit var ivAgua: ImageView
    private lateinit var corazones: List<ImageView>

    private var puntaje = 0
    private var vidas = 3
    private lateinit var fase: String

    // estados de selección
    private var selBotiquin = false
    private var selLinterna = false
    private var selPelota = false
    private var selAgua = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_objetos)

        // Recuperar datos de la trivia
        puntaje = intent.getIntExtra("puntajeActual", 0)
        vidas = intent.getIntExtra("vidasActuales", 3)
        fase = intent.getStringExtra("faseSeleccionada") ?: "antes"

        tvPuntaje = findViewById(R.id.tvPuntaje)
        tvVidas = findViewById(R.id.tvPuntaje) // tu layout no tiene tvVidas; reutilizo tvPuntaje para mostrar
        // Si tienes un TextView específico para fase, descomenta la siguiente línea y coméntala arriba:
        confettiView = findViewById(R.id.confettiView)
        btnConfirmar = findViewById(R.id.Confirmar)

        ivBotiquin = findViewById(R.id.Botiquin)
        ivAgua = findViewById(R.id.Agua)
        ivLinterna = findViewById(R.id.Linterna)
        ivPelota = findViewById(R.id.Pelota)

        corazones = listOf(
            findViewById(R.id.corazon1),
            findViewById(R.id.corazon2),
            findViewById(R.id.corazon3)
        )

        // toggle simple: al tocar la imagen, alterna selección (cambia alpha)
        ivBotiquin.setOnClickListener { selBotiquin = toggleSelection(ivBotiquin, selBotiquin) }
        ivAgua.setOnClickListener { selAgua = toggleSelection(ivAgua, selAgua) }
        ivLinterna.setOnClickListener { selLinterna = toggleSelection(ivLinterna, selLinterna) }
        ivPelota.setOnClickListener { selPelota = toggleSelection(ivPelota, selPelota) }

        actualizarUI()

        btnConfirmar.setOnClickListener {
            verificarRespuestas()
        }
    }

    private fun toggleSelection(iv: ImageView, current: Boolean): Boolean {
        val nuevo = !current
        iv.alpha = if (nuevo) 0.6f else 1f
        return nuevo
    }

    private fun actualizarUI() {
        tvPuntaje.text = "Puntaje: $puntaje"
        // Si tienes un TextView para vidas lo actualizas aquí; tu layout tiene corazones en la parte superior
        for (i in corazones.indices) {
            corazones[i].setImageResource(
                if (i < vidas) R.drawable.corazon_lleno else R.drawable.corazon_vacio
            )
        }
        // tvFase?.text = "Fase: ${fase.uppercase()}"
    }

    private fun verificarRespuestas() {
        // Lógica original: botiquín y agua deben estar seleccionados, pelota no seleccionada
        val seleccionCorrecta = selBotiquin && selAgua && !selPelota

        if (seleccionCorrecta) {
            puntaje += 10
            mostrarConfetti()
            Toast.makeText(this, "¡Muy bien! Objetos correctos", Toast.LENGTH_SHORT).show()
        } else {
            vidas--
            Toast.makeText(this, "¡Oops! Algunos objetos no son útiles", Toast.LENGTH_SHORT).show()
        }

        actualizarUI()

        // Preparamos el Intent de resultado para devolver al juego principal
        val result = Intent().apply {
            putExtra("puntajeResult", puntaje)
            putExtra("vidasResult", vidas)
            putExtra("gameOver", vidas <= 0)
        }
        // Si vidas <= 0 devolvemos gameOver=true para que JuegoActivity termine el juego
        setResult(Activity.RESULT_OK, result)

        // Si vidas <= 0, podemos reproducir sonido de perder y finalizar
        if (vidas <= 0) {
            val loseSound = MediaPlayer.create(this, R.raw.perder)
            loseSound.start()
            // damos un pequeño delay visual por si acaso (opcional), pero finalizamos inmediatamente
            finish()
            return
        }

        // En la integración tipo B, después de confirmar devolvemos el resultado y cerramos la activity
        finish()
    }

    private fun mostrarConfetti() {
        confettiView.visibility = View.VISIBLE
        confettiView.playAnimation()
        confettiView.addAnimatorUpdateListener {
            if (!confettiView.isAnimating) confettiView.visibility = View.GONE
        }
    }

}
