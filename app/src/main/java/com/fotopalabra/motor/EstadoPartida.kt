package com.fotopalabra.motor

/** Una letra disponible para tocar. [deLaSolucion] indica si forma parte de la respuesta. */
data class Ficha(
    val id: Int,
    val letra: Char,
    val deLaSolucion: Boolean,
    val eliminada: Boolean = false,
)

/** Un casillero de la respuesta. Si [revelado] es verdadero fue completado por una pista y queda fijo. */
data class Casillero(
    val fichaId: Int? = null,
    val revelado: Boolean = false,
)

enum class Resultado { INCOMPLETO, CORRECTO, INCORRECTO }

/** Estado inmutable de un nivel en curso. Todas las operaciones están en [MotorJuego]. */
data class EstadoPartida(
    val solucion: String,
    val fichas: List<Ficha>,
    val casilleros: List<Casillero>,
) {
    val fichasUsadas: Set<Int> by lazy { casilleros.mapNotNull { it.fichaId }.toSet() }

    fun ficha(id: Int): Ficha = fichas.first { it.id == id }

    fun letraEn(indice: Int): Char? = casilleros[indice].fichaId?.let { ficha(it).letra }

    val completo: Boolean get() = casilleros.all { it.fichaId != null }

    val palabraActual: String get() = casilleros.indices.joinToString("") { (letraEn(it) ?: '_').toString() }

    fun estaDisponible(ficha: Ficha): Boolean = !ficha.eliminada && ficha.id !in fichasUsadas
}
