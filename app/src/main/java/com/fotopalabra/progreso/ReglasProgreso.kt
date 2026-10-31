package com.fotopalabra.progreso

import com.fotopalabra.monedas.Billetera
import com.fotopalabra.niveles.CatalogoNiveles
import com.fotopalabra.niveles.Nivel
import com.fotopalabra.pistas.PistasNivel

/** Resultado de completar un nivel. */
data class NivelCompletado(
    val progreso: Progreso,
    val monedasGanadas: Int,
    val primeraVez: Boolean,
)

/** Reglas de desbloqueo y registro de niveles completados. */
object ReglasProgreso {

    fun estaDesbloqueado(progreso: Progreso, nivel: Nivel): Boolean =
        nivel.numero <= progreso.desbloqueadoHasta || nivel.id in progreso.completados

    fun estaCompletado(progreso: Progreso, nivel: Nivel): Boolean = nivel.id in progreso.completados

    /** Nivel con el que continúa el botón JUGAR: el primero desbloqueado sin resolver. */
    fun nivelActual(progreso: Progreso, catalogo: CatalogoNiveles): Nivel? =
        catalogo.niveles.firstOrNull { estaDesbloqueado(progreso, it) && !estaCompletado(progreso, it) }

    fun todoCompletado(progreso: Progreso, catalogo: CatalogoNiveles): Boolean =
        catalogo.niveles.isNotEmpty() && catalogo.niveles.all { it.id in progreso.completados }

    fun completar(progreso: Progreso, nivel: Nivel): NivelCompletado {
        val primeraVez = nivel.id !in progreso.completados
        val pistas = progreso.pistasDe(nivel.id)
        val usoPistas = pistas.cantidad > 0
        val monedas = Billetera.recompensa(nivel, !primeraVez, usoPistas)
        val est = progreso.estadisticas
        val racha = if (primeraVez && !usoPistas) est.rachaActual + 1 else if (primeraVez) 0 else est.rachaActual
        val actualizado = progreso.copy(
            desbloqueadoHasta = maxOf(progreso.desbloqueadoHasta, nivel.numero + 1),
            completados = progreso.completados + nivel.id,
            pistas = progreso.pistas - nivel.id,
            estadisticas = est.copy(
                nivelesSinPistas = est.nivelesSinPistas + if (primeraVez && !usoPistas) 1 else 0,
                rachaActual = racha,
                mejorRacha = maxOf(est.mejorRacha, racha),
            ),
        )
        return NivelCompletado(Billetera.acreditar(actualizado, monedas), monedas, primeraVez)
    }

    fun registrarIntentoFallido(progreso: Progreso): Progreso =
        progreso.copy(estadisticas = progreso.estadisticas.copy(intentosFallidos = progreso.estadisticas.intentosFallidos + 1))

    fun guardarPistas(progreso: Progreso, nivelId: Int, pistas: PistasNivel): Progreso =
        progreso.copy(pistas = progreso.pistas + (nivelId to pistas))
}
