package com.example.rdekids

object IAHelper {

    // Mensajes simples - por el momento
    fun mensaje(correcta: Boolean, fase: String = "durante"): String {
        return if (correcta) {
            when (fase) {
                "antes" -> listOf("¡Excelente reflexo!", "Bien pensado — sigue así", "¡Muy bien!").random()
                "durante" -> listOf("¡Buen trabajo protegiéndote!", "¡Estás haciendo lo correcto!", "¡Perfecto!").random()
                "despues" -> listOf("Buen chequeo post-sismo", "Excelente ayuda a los demás", "Muy responsable").random()
                else -> listOf("¡Buen trabajo!").random()
            }
        } else {
            when (fase) {
                "antes" -> listOf("Revisa tu kit de emergencia.", "Asegura objetos pesados.", "Haz un plan con tu familia.").random()
                "durante" -> listOf("Baja y cúbrete, no corras.", "Protégete bajo una mesa.", "Aléjate de ventanas.").random()
                "despues" -> listOf("Revisa posibles daños.", "No regresar al edificio aún.", "Espera instrucciones").random()
                else -> listOf("Inténtalo otra vez").random()
            }
        }
    }

    // Sube dificultad según puntaje: nivel = puntaje / 30 + 1
    fun nivelDesdePuntaje(puntaje: Int): Int = (puntaje / 30) + 1
}
