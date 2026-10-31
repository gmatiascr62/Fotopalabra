package com.fotopalabra.niveles

/** Dificultad de un nivel. Define la recompensa en monedas. */
enum class Dificultad(val etiqueta: String, val recompensa: Int) {
    FACIL("Fácil", 10),
    INTERMEDIO("Intermedio", 15),
    DIFICIL("Difícil", 25);

    companion object {
        fun desdeTexto(texto: String): Dificultad? = when (
            com.fotopalabra.motor.Texto.normalizar(texto)
        ) {
            "FACIL" -> FACIL
            "INTERMEDIO", "MEDIO" -> INTERMEDIO
            "DIFICIL" -> DIFICIL
            else -> null
        }
    }
}

/** Una imagen del nivel. [archivo] es relativo a `assets/niveles/`. */
data class Imagen(
    val archivo: String,
    val descripcion: String,
)

/**
 * Nivel ya validado y listo para jugar.
 *
 * @property numero posición del nivel en el juego (1, 2, 3…), según el orden de [id].
 * @property respuesta texto tal como se mostrará al acertar (con tildes).
 * @property solucion respuesta normalizada: mayúsculas, sin tildes ni espacios.
 */
data class Nivel(
    val id: Int,
    val numero: Int,
    val respuesta: String,
    val solucion: String,
    val dificultad: Dificultad,
    val imagenes: List<Imagen>,
    val pista: String,
    val fichas: Int?,
)
