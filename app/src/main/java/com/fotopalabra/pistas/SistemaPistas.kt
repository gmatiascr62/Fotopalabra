package com.fotopalabra.pistas

import com.fotopalabra.motor.Casillero
import com.fotopalabra.motor.EstadoPartida
import com.fotopalabra.motor.reemplazarCasillero
import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class TipoPista(val titulo: String, val descripcion: String, val costo: Int) {
    REVELAR_LETRA("Revelar letra", "Coloca una letra correcta en su lugar.", 25),
    ELIMINAR_LETRAS("Quitar letras", "Elimina todas las letras que sobran.", 40),
}

/** Pistas ya compradas en un nivel. Se guardan para no perderlas al cerrar la app. */
@Serializable
data class PistasNivel(
    val letrasEliminadas: Boolean = false,
    val reveladas: List<Int> = emptyList(),
) {
    val cantidad: Int get() = (if (letrasEliminadas) 1 else 0) + reveladas.size
}

/** Aplica las pistas sobre el estado de la partida. No cobra: eso lo hace la billetera. */
object SistemaPistas {

    fun disponible(tipo: TipoPista, estado: EstadoPartida, pistas: PistasNivel): Boolean =
        when (tipo) {
            TipoPista.ELIMINAR_LETRAS -> !pistas.letrasEliminadas && estado.fichas.any { !it.deLaSolucion && !it.eliminada }
            // Siempre debe quedar al menos una letra para que el jugador la complete.
            TipoPista.REVELAR_LETRA -> estado.casilleros.count { !it.revelado } > 1
        }

    /** Elige al azar un casillero no revelado (priorizando los incorrectos) y coloca la letra correcta. */
    fun revelarLetra(estado: EstadoPartida, random: Random = Random.Default): Pair<EstadoPartida, Int>? {
        val candidatos = estado.casilleros.indices.filter { !estado.casilleros[it].revelado }
        if (candidatos.isEmpty()) return null
        val incorrectos = candidatos.filter { estado.letraEn(it) != estado.solucion[it] }
        val posicion = (incorrectos.ifEmpty { candidatos }).random(random)
        return revelarEn(estado, posicion) to posicion
    }

    /** Fija la letra correcta en [posicion]. También se usa para restaurar pistas guardadas. */
    fun revelarEn(estado: EstadoPartida, posicion: Int): EstadoPartida {
        val actual = estado.casilleros.getOrNull(posicion) ?: return estado
        if (actual.revelado) return estado
        val letra = estado.solucion[posicion]

        // Si ya tiene la letra correcta, solo se fija.
        if (actual.fichaId != null && estado.ficha(actual.fichaId).letra == letra) {
            return estado.reemplazarCasillero(posicion, actual.copy(revelado = true))
        }

        var e = estado.reemplazarCasillero(posicion, Casillero())
        val libre = e.fichas
            .filter { it.letra == letra && e.estaDisponible(it) }
            .sortedByDescending { it.deLaSolucion }
            .firstOrNull()

        val fichaId = if (libre != null) {
            libre.id
        } else {
            // La letra necesaria está en otro casillero equivocado: se la mueve.
            val origen = e.casilleros.indices.firstOrNull { i ->
                val c = e.casilleros[i]
                !c.revelado && c.fichaId != null && e.ficha(c.fichaId).letra == letra && e.solucion[i] != letra
            } ?: e.casilleros.indices.firstOrNull { i ->
                val c = e.casilleros[i]
                !c.revelado && c.fichaId != null && e.ficha(c.fichaId).letra == letra
            } ?: return estado
            val id = e.casilleros[origen].fichaId!!
            e = e.reemplazarCasillero(origen, Casillero())
            id
        }
        return e.reemplazarCasillero(posicion, Casillero(fichaId, revelado = true))
    }

    /** Elimina todas las fichas de relleno. Las que estaban colocadas vuelven a quedar libres. */
    fun eliminarLetras(estado: EstadoPartida): EstadoPartida {
        var e = estado
        // Si una ficha de relleno ocupa un casillero con la letra correcta, se cambia por
        // la ficha equivalente de la solución para no borrarle un acierto al jugador.
        e.casilleros.forEachIndexed { i, c ->
            val id = c.fichaId ?: return@forEachIndexed
            val ficha = e.ficha(id)
            if (!ficha.deLaSolucion && ficha.letra == e.solucion[i]) {
                val reemplazo = e.fichas.firstOrNull { it.deLaSolucion && it.letra == ficha.letra && e.estaDisponible(it) }
                if (reemplazo != null) e = e.reemplazarCasillero(i, c.copy(fichaId = reemplazo.id))
            }
        }
        val relleno = e.fichas.filter { !it.deLaSolucion }.map { it.id }.toSet()
        return e.copy(
            fichas = e.fichas.map { if (it.id in relleno) it.copy(eliminada = true) else it },
            casilleros = e.casilleros.map { if (it.fichaId in relleno) Casillero() else it },
        )
    }

    /** Vuelve a aplicar sobre una partida nueva las pistas compradas anteriormente. */
    fun restaurar(estado: EstadoPartida, pistas: PistasNivel): EstadoPartida {
        var e = estado
        if (pistas.letrasEliminadas) e = eliminarLetras(e)
        pistas.reveladas.filter { it in e.casilleros.indices }.forEach { e = revelarEn(e, it) }
        return e
    }
}
