package com.example.rdekids.iu.juego

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.airbnb.lottie.LottieAnimationView
import com.example.rdekids.local.dao.JuegoDAO
import com.example.rdekids.local.AppDatabase
import com.example.rdekids.local.entities.Puntaje
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.session.SessionManager
import com.example.rdekids.worker.SyncWorker
import com.example.rdekids.iu.utils.IAHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rdekids.R
import com.example.rdekids.iu.login.MainActivity

class JuegoActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // UI
    private lateinit var tvPuntaje: TextView
    private lateinit var tvNivel: TextView
    private lateinit var tvPregunta: TextView
    private lateinit var tvDialogo: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var btn1: AppCompatButton
    private lateinit var btn2: AppCompatButton
    private lateinit var btn3: AppCompatButton
    private lateinit var btnPausar: AppCompatButton
    private lateinit var btnTerminar: AppCompatButton
    private lateinit var confettiView: LottieAnimationView
    private lateinit var corazones: List<ImageView>

    // Estado del juego
    private var puntaje = 0
    private var vidas = 3
    private var aciertos = 0
    private var puntero = 0
    private var estaEnPausa = false

    private lateinit var dao: JuegoDAO
    private lateinit var tts: TextToSpeech
    private var mediaPlayer: MediaPlayer? = null

    // Modelo de preguntas (texto o imagen)
    data class Pregunta(
        val id: String = UUID.randomUUID().toString(),
        val tipo: TipoPregunta = TipoPregunta.TEXTO,
        val pregunta: String? = null,           // para TEXTO: enunciado
        val opciones: List<String> = emptyList(), // para TEXTO: opciones
        val correcta: Int = 0,                 // índice de opción correcta (cuando aplica)
        val fase: String = ""                  // fase (antes/durante/despues) u otra metadata
    )

    enum class TipoPregunta { TEXTO, IMAGEN }

    private val preguntas = mutableListOf<Pregunta>()
    private lateinit var orden: MutableList<Int>

    // Launcher para recibir resultado de SeleccionObjetosActivity
    private val seleccionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            // Si la activity fue cancelada sin respuesta, tratamos como no penalizar y avanzamos.
            avanzarYSolicitarSiguiente()
            return@registerForActivityResult
        }

        val data = result.data
        if (data == null) {
            avanzarYSolicitarSiguiente()
            return@registerForActivityResult
        }

        // Obtener resultados devueltos por la activity de selección
        val puntajeReturned = data.getIntExtra("puntajeResult", puntaje)
        val vidasReturned = data.getIntExtra("vidasResult", vidas)
        val gameOver = data.getBooleanExtra("gameOver", false)

        // Actualizar estado global con lo devuelto
        puntaje = puntajeReturned
        vidas = vidasReturned

        // Actualizar UI
        tvPuntaje.text = "Puntaje: $puntaje"
        tvNivel.text = "Nivel ${IAHelper.nivelDesdePuntaje(puntaje)}"
        actualizarVidas()

        // Si la selección indicó gameOver o vidas <= 0, terminar el juego
        if (gameOver || vidas <= 0) {
            terminarJuego()
            return@registerForActivityResult
        }

        // Avanzar al siguiente
        avanzarYSolicitarSiguiente()
    }

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
        btnPausar = findViewById(R.id.btnPausar)
        btnTerminar = findViewById(R.id.btnTerminar)
        confettiView = findViewById(R.id.confettiView)

        corazones = listOf(
            findViewById(R.id.corazon1),
            findViewById(R.id.corazon2),
            findViewById(R.id.corazon3)
        )

        // Música de fondo (opcional)
        mediaPlayer = MediaPlayer.create(this, R.raw.jazz)
        mediaPlayer?.isLooping = true
        val sonidoActivo = getSharedPreferences("ConfiguracionJuego", MODE_PRIVATE)
            .getBoolean("sonido", true)
        if (sonidoActivo) mediaPlayer?.start()

        // Inicializar preguntas (aquí debes añadir todas tus preguntas, incluyendo las IMAGEN)
        prepararPreguntasIniciales()

        // Preparar orden aleatorio
        reshuffleOrden()

        // Click listeners para botones de respuesta de preguntas TEXTO
        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        btn1.setOnClickListener { it.startAnimation(anim); onRespuestaBoton(0) }
        btn2.setOnClickListener { it.startAnimation(anim); onRespuestaBoton(1) }
        btn3.setOnClickListener { it.startAnimation(anim); onRespuestaBoton(2) }

        // Listeners para pausa/terminar
        btnPausar.setOnClickListener { mostrarMenuPausa() }
        btnTerminar.setOnClickListener { mostrarConfirmacionTerminar() }

        // Parallax (si lo tienes)
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

        // Mostrar la primera pregunta
        mostrarSiguiente()
    }

    // ---------------- PAUSA / TERMINAR / REINICIAR ----------------

    private fun mostrarMenuPausa() {
        if (estaEnPausa) return
        estaEnPausa = true

        // Detener sonidos / animaciones / TTS
        mediaPlayer?.pause()
        if (::confettiView.isInitialized) {
            try { confettiView.pauseAnimation() } catch (e: Exception) { /* ignore */ }
        }
        if (::tts.isInitialized) {
            try { tts.stop() } catch (e: Exception) { /* ignore */ }
        }

        // Deshabilitar inputs
        deshabilitarInputs()

        val opciones = arrayOf("Reanudar", "Reiniciar partida", "Salir al menú")
        AlertDialog.Builder(this)
            .setTitle("Juego en pausa")
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> { // Reanudar
                        dialog.dismiss()
                        reanudarJuego()
                    }
                    1 -> { // Reiniciar partida
                        dialog.dismiss()
                        reiniciarPartida()
                    }
                    2 -> { // Salir al menú
                        dialog.dismiss()
                        // Llamar terminarJuego para guardar y salir de forma consistente
                        terminarJuego()
                    }
                }
            }
            .setOnCancelListener {
                // Si el diálogo se cancela (back), reanudar automáticamente
                reanudarJuego()
            }
            .show()
    }

    private fun mostrarConfirmacionTerminar() {
        // Diálogo de confirmación: si confirma, llamamos terminarJuego()
        AlertDialog.Builder(this)
            .setTitle("Terminar partida")
            .setMessage("¿Seguro que deseas terminar la partida? Se perderá el progreso actual.")
            .setPositiveButton("Sí, terminar") { dialog, _ ->
                dialog.dismiss()
                terminarJuego()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun reiniciarPartida() {
        // Restaurar estado inicial
        puntaje = 0
        vidas = 3
        aciertos = 0
        puntero = 0
        // Reordenar preguntas
        preguntas.shuffle()
        reshuffleOrden()
        // Actualizar UI
        tvPuntaje.text = "Puntaje: $puntaje"
        tvNivel.text = "Nivel ${IAHelper.nivelDesdePuntaje(puntaje)}"
        actualizarVidas()
        // Reanudar si estaba en pausa
        reanudarJuego()
        // Cargar la primera pregunta
        mostrarSiguiente()
    }

    private fun reanudarJuego() {
        estaEnPausa = false
        // Reanudar sonidos y animaciones
        val sonidoActivo = getSharedPreferences("ConfiguracionJuego", MODE_PRIVATE)
            .getBoolean("sonido", true)
        if (sonidoActivo) {
            try { mediaPlayer?.start() } catch (e: Exception) { /* ignore */ }
        }
        if (::confettiView.isInitialized) {
            try { confettiView.resumeAnimation() } catch (e: Exception) { /* ignore */ }
        }
        // Habilitar inputs
        habilitarInputs()
    }

    private fun deshabilitarInputs() {
        btn1.isEnabled = false
        btn2.isEnabled = false
        btn3.isEnabled = false
        btnPausar.isEnabled = false
        btnTerminar.isEnabled = false
    }

    private fun habilitarInputs() {
        btn1.isEnabled = true
        btn2.isEnabled = true
        btn3.isEnabled = true
        btnPausar.isEnabled = true
        btnTerminar.isEnabled = true
    }

    // ---------------------------------------------------------------

    private fun prepararPreguntasIniciales() {
        // --- Preguntas de texto (ejemplos) ---
        preguntas.clear()
        preguntas.add(
            Pregunta(
                tipo = TipoPregunta.TEXTO,
                pregunta = "¿Qué debe contener una mochila de emergencia?",
                opciones = listOf("Juguetes", "Botiquín", "Ropa sucia"),
                correcta = 1,
                fase = "antes"
            )
        )
        preguntas.add(
            Pregunta(
                tipo = TipoPregunta.TEXTO,
                pregunta = "Durante un sismo, ¿qué debes hacer?",
                opciones = listOf("Correr afuera", "Cubrirte y agacharte", "Saltar"),
                correcta = 1,
                fase = "durante"
            )
        )
        preguntas.add(
            Pregunta(
                tipo = TipoPregunta.TEXTO,
                pregunta = "Después del sismo, debes:",
                opciones = listOf("Revisar heridos", "Encender fogatas", "Irse sin chequear"),
                correcta = 0,
                fase = "despues"
            )
        )

        // --- Preguntas tipo IMAGEN (representan que hay que lanzar SeleccionObjetosActivity) ---
        preguntas.add(
            Pregunta(
                tipo = TipoPregunta.IMAGEN,
                pregunta = "Selecciona los objetos útiles para una mochila de emergencia",
                correcta = 0,
                fase = "antes"
            )
        )
    }

    private fun reshuffleOrden() {
        orden = (preguntas.indices).toMutableList()
        orden.shuffle()
        puntero = 0
    }

    private fun obtenerPreguntaActual(): Pregunta? {
        if (!::orden.isInitialized || orden.isEmpty()) return null
        if (puntero >= orden.size) {
            // Si ya recorrimos todas las preguntas las volvemos a barajar
            reshuffleOrden()
        }
        val idx = orden[puntero]
        return preguntas.getOrNull(idx)
    }

    private fun mostrarSiguiente() {
        // Condición de fin: 3 aciertos o 0 vidas
        if (aciertos >= 3 || vidas <= 0) {
            terminarJuego()
            return
        }

        val q = obtenerPreguntaActual() ?: run {
            terminarJuego()
            return
        }

        if (q.tipo == TipoPregunta.TEXTO) {
            mostrarPreguntaTexto(q)
        } else {
            // No mostramos imagen en esta activity; lanzamos SeleccionObjetosActivity
            lanzarSeleccionObjetos(q)
        }
    }

    private fun mostrarPreguntaTexto(q: Pregunta) {
        // Asegurarse que la UI de texto está visible
        tvPregunta.visibility = View.VISIBLE
        btn1.visibility = View.VISIBLE
        btn2.visibility = View.VISIBLE
        btn3.visibility = View.VISIBLE

        tvPregunta.text = q.pregunta
        btn1.text = q.opciones.getOrNull(0) ?: ""
        btn2.text = q.opciones.getOrNull(1) ?: ""
        btn3.text = q.opciones.getOrNull(2) ?: ""

        hablar(q.pregunta ?: "")
        imgAvatar.setImageResource(R.drawable.avatar_neutral)
        tvDialogo.text = "¿Qué harías?"
        tvNivel.text = "Nivel ${IAHelper.nivelDesdePuntaje(puntaje)}"
        tvPuntaje.text = "Puntaje: $puntaje"
    }

    private fun lanzarSeleccionObjetos(q: Pregunta) {
        val intent = Intent(this, com.example.rdekids.iu.juego.SeleccionObjetosActivity::class.java).apply {
            putExtra("puntajeActual", puntaje)
            putExtra("vidasActuales", vidas)
            putExtra("faseSeleccionada", q.fase)
        }
        seleccionLauncher.launch(intent)
    }

    private fun onRespuestaBoton(index: Int) {
        val q = obtenerPreguntaActual() ?: return
        if (q.tipo != TipoPregunta.TEXTO) return

        val correcta = index == q.correcta
        manejarRespuesta(correcta, q.fase)
        avanzarYSolicitarSiguiente()
    }

    private fun avanzarYSolicitarSiguiente() {
        // Avanzar el puntero después de procesar la respuesta
        puntero++
        // Actualizar UI de vidas
        actualizarVidas()
        // Revisar fin
        if (aciertos >= 3 || vidas <= 0) {
            terminarJuego()
            return
        }
        // Mostrar siguiente pregunta
        mostrarSiguiente()
    }

    private fun manejarRespuesta(correcta: Boolean, fase: String) {
        val msg = IAHelper.mensaje(correcta, fase)
        animarTexto(tvDialogo, msg)
        hablar(msg)

        imgAvatar.setImageResource(if (correcta) R.drawable.avatar_feliz else R.drawable.avatar_triste)

        if (correcta) {
            puntaje += 10
            aciertos++
        } else {
            puntaje = (puntaje - 5).coerceAtLeast(0)
            vidas--
            if (vidas in 0 until corazones.size) {
                animarCorazonPerdido(vidas)
            } else {
                actualizarVidas()
            }
        }

        tvPuntaje.text = "Puntaje: $puntaje"
        tvNivel.text = "Nivel ${IAHelper.nivelDesdePuntaje(puntaje)}"

        // Guardado y sincronización (igual que antes)
        dao.guardarPuntaje(puntaje)
        val usuarioActual = SessionManager.obtenerUsuario(this) ?: "Invitado"
        GoogleSheetsService.enviarDatos(usuarioActual, puntaje)

        val p = Puntaje(usuario = usuarioActual, puntaje = puntaje, synced = false)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(this@JuegoActivity)
                    .puntajeDao()
                    .insert(p)
            } catch (e: Exception) {
                Log.e("JuegoActivity", "Error guardando puntaje: ${e.message}")
            }
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueue(syncRequest)
    }

    private fun animarCorazonPerdido(index: Int) {
        val corazon = corazones[index]
        val anim = AnimationUtils.loadAnimation(this, R.anim.corazon_perdido)
        corazon.startAnimation(anim)
        corazon.postDelayed({
            corazon.setImageResource(R.drawable.corazon_vacio)
        }, 800)
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

        val p = Puntaje(usuario = usuarioActual, puntaje = puntaje, synced = false)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(this@JuegoActivity)
                    .puntajeDao()
                    .insert(p)
            } catch (e: Exception) {
                Log.e("JuegoActivity", "Error guardando puntaje: ${e.message}")
            }
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueue(syncRequest)

        val puntajeMaximo = preguntas.size * 10
        if (puntaje == puntajeMaximo) {
            mostrarConfetti()
            Toast.makeText(this, "¡Perfecto! Puntaje máximo: $puntaje", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "¡Juego terminado! Puntaje: $puntaje", Toast.LENGTH_LONG).show()
        }

        // Volver al inicio (ajusta la activity si tu pantalla inicial no es MainActivity)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
        } else {
            Log.e("JuegoActivity", "Error al inicializar TextToSpeech")
        }
    }

    private fun actualizarVidas() {
        for (i in corazones.indices) {
            corazones[i].setImageResource(
                if (i < vidas) R.drawable.corazon_lleno else R.drawable.corazon_vacio
            )
        }
    }

    private fun animarTexto(tv: TextView, texto: String, delay: Long = 35L) {
        tv.text = ""
        var i = 0
        val handler = Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (i < texto.length) {
                    tv.text = texto.substring(0, i + 1)
                    i++
                    handler.postDelayed(this, delay)
                }
            }
        }
        handler.post(runnable)
    }

    private fun hablar(text: String) {
        if (!::tts.isInitialized) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}
