package com.fotopalabra.progreso

import com.fotopalabra.monedas.Billetera
import com.fotopalabra.pistas.PistasNivel
import kotlinx.serialization.Serializable

@Serializable
data class Estadisticas(
    val monedasGanadas: Int = 0,
    val monedasGastadas: Int = 0,
    val pistasUsadas: Int = 0,
    val intentosFallidos: Int = 0,
    val nivelesSinPistas: Int = 0,
    val rachaActual: Int = 0,
    val mejorRacha: Int = 0,
)

@Serializable
data class Ajustes(
    val avanceAutomatico: Boolean = true,
    val vibracion: Boolean = true,
    /** Modo prueba: permite jugar cualquier nivel sin haber resuelto los anteriores. */
    val desbloquearTodo: Boolean = false,
)

/**
 * Todo lo que se guarda en el teléfono.
 *
 * @property desbloqueadoHasta número (1, 2, 3…) del último nivel desbloqueado.
 * @property completados ids de los niveles resueltos.
 * @property pistas pistas compradas en niveles todavía no resueltos.
 */
@Serializable
data class Progreso(
    val monedas: Int = Billetera.MONEDAS_INICIALES,
    val desbloqueadoHasta: Int = 1,
    val completados: Set<Int> = emptySet(),
    val pistas: Map<Int, PistasNivel> = emptyMap(),
    val estadisticas: Estadisticas = Estadisticas(),
    val ajustes: Ajustes = Ajustes(),
) {
    fun pistasDe(nivelId: Int): PistasNivel = pistas[nivelId] ?: PistasNivel()
}
