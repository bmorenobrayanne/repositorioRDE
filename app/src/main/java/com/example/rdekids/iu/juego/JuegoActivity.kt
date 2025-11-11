package com.example.rdekids.iu.juego

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.airbnb.lottie.LottieAnimationView
import com.example.rdekids.iu.utils.IAHelper
import com.example.rdekids.R
import com.example.rdekids.local.dao.JuegoDAO
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.session.SessionManager
import java.util.Locale

class JuegoActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var dao: JuegoDAO
    private lateinit var tvPuntaje: TextView
    private lateinit var tvNivel: TextView
    private lateinit var tvPregunta: TextView
    private lateinit var tvDialogo: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var btn1: AppCompatButton
    private lateinit var btn2: AppCompatButton
    private lateinit var btn3: AppCompatButton
    private lateinit var confettiView: LottieAnimationView

    private var puntaje = 0
    private var nivel = 1
    private lateinit var tts: TextToSpeech
    private var mediaPlayer: MediaPlayer? = null

    //Lista de preguntas por el momento
    private val preguntas = mutableListOf(
        Quad("¿Qué debe contener una mochila de emergencia?", listOf("Juguetes", "Botiquín", "Ropa sucia"), 1, "antes"),
        Quad("Durante un sismo, ¿qué debes hacer?", listOf("Correr afuera", "Cubrirte y agacharte", "Saltar"), 1, "durante"),
        Quad("Después del sismo, debes:", listOf("Revisar heridos", "Encender fogatas", "Irse sin chequear"), 0, "despues")
    )
    private var actual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        dao = JuegoDAO(this)
        tts = TextToSpeech(this, this)

        tvPuntaje = findViewById(R.id.tvPuntaje)
        tvNivel = findViewById(R.id.tvNivel)
        tvPregunta = findViewById(R.id.tvPregunta)
        tvDialogo = findViewById(R.id.tvDialogo)
        imgAvatar = findViewById(R.id.imgAvatar)
        btn1 = findViewById(R.id.btnRespuesta1)
        btn2 = findViewById(R.id.btnRespuesta2)
        btn3 = findViewById(R.id.btnRespuesta3)
        confettiView = findViewById(R.id.confettiView)

        //Música de fondo
        mediaPlayer = MediaPlayer.create(this, R.raw.jazz)
        mediaPlayer?.isLooping = true
        val sonidoActivo = getSharedPreferences("ConfiguracionJuego", MODE_PRIVATE).getBoolean("sonido", true)
        if (sonidoActivo) mediaPlayer?.start()

        //Mostrar primera pregunta
        mostrarPregunta()

        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)

        val respuestaClick: (Int) -> Unit = { index ->
            val q = preguntas[actual]
            val correcta = index == q.correct
            manejarRespuesta(correcta, q.fase)
            actual++
            if (actual < preguntas.size) {
                mostrarPregunta()
            } else {
                terminarJuego()
            }
        }

        btn1.setOnClickListener { it.startAnimation(anim); respuestaClick(0) }
        btn2.setOnClickListener { it.startAnimation(anim); respuestaClick(1) }
        btn3.setOnClickListener { it.startAnimation(anim); respuestaClick(2) }

        //Parallax simple - la profundidad que se genera al sostenos unos de los botones
        val parallax = findViewById<View>(R.id.parallaxBg)
        var lastX = 0f
        parallax.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.x - lastX
                    v.translationX = (v.translationX + dx / 40).coerceIn(-50f, 50f)
                    lastX = ev.x
                }
                MotionEvent.ACTION_DOWN -> lastX = ev.x
                MotionEvent.ACTION_UP -> v.animate().translationX(0f).setDuration(300).start()
            }
            true
        }
    }

    private fun mostrarPregunta() {
        val q = preguntas[actual]
        tvPregunta.text = q.pregunta
        btn1.text = q.opciones[0]
        btn2.text = q.opciones[1]
        btn3.text = q.opciones[2]
        hablar(q.pregunta)
        imgAvatar.setImageResource(R.drawable.avatar_neutral)
        tvDialogo.text = "¿Qué harías?"
        tvNivel.text = "Nivel ${IAHelper.nivelDesdePuntaje(puntaje)}"
        tvPuntaje.text = "Puntaje: $puntaje"
    }

    private fun manejarRespuesta(correcta: Boolean, fase: String) {
        val msg = IAHelper.mensaje(correcta, fase)
        tvDialogo.text = msg
        hablar(msg)

        imgAvatar.setImageResource(if (correcta) R.drawable.avatar_feliz else R.drawable.avatar_triste)

        if (correcta) {
            puntaje += 10
            if (puntaje % 30 == 0) mostrarConfetti()
        } else {
            puntaje = (puntaje - 5).coerceAtLeast(0)
        }

        tvPuntaje.text = "Puntaje: $puntaje"
        tvNivel.text = "Nivel ${IAHelper.nivelDesdePuntaje(puntaje)}"

        dao.guardarPuntaje(puntaje)

        val usuarioActual = SessionManager.obtenerUsuario(this) ?: "Invitado"
        GoogleSheetsService.enviarDatos(usuarioActual, puntaje)
    }


    private fun mostrarConfetti() {
        confettiView.visibility = View.VISIBLE
        confettiView.playAnimation()
        confettiView.addAnimatorUpdateListener {
            if (!confettiView.isAnimating) confettiView.visibility = View.GONE
        }
    }

    private fun terminarJuego() {
        val victory = MediaPlayer.create(this, R.raw.victoria)
        victory.start()

        dao.guardarPartida()

        val usuarioActual = SessionManager.obtenerUsuario(this) ?: "Invitado"
        GoogleSheetsService.enviarDatos(usuarioActual, puntaje)

        Toast.makeText(this, "¡Juego terminado! Puntaje: $puntaje", Toast.LENGTH_LONG).show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
        }
    }

    private fun hablar(text: String) {
        if (!::tts.isInitialized) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
    data class Quad(val pregunta: String, val opciones: List<String>, val correct: Int, val fase: String)
}