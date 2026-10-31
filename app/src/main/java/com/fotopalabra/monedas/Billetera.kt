package com.fotopalabra.monedas

import com.fotopalabra.niveles.Nivel
import com.fotopalabra.progreso.Progreso

/** Sistema de monedas: cuánto se gana por nivel y cómo se cobran las pistas. */
object Billetera {

    const val MONEDAS_INICIALES = 100

    /** Bonificación por resolver el nivel sin usar pistas. */
    const val BONO_SIN_PISTAS = 5

    /** Monedas que da un nivel. Repetir un nivel ya completado no da monedas. */
    fun recompensa(nivel: Nivel, yaCompletado: Boolean, usoPistas: Boolean): Int = when {
        yaCompletado -> 0
        usoPistas -> nivel.dificultad.recompensa
        else -> nivel.dificultad.recompensa + BONO_SIN_PISTAS
    }

    fun puedePagar(progreso: Progreso, costo: Int): Boolean = progreso.monedas >= costo

    /** Devuelve el progreso con las monedas descontadas, o null si no alcanzan. */
    fun cobrar(progreso: Progreso, costo: Int): Progreso? {
        if (!puedePagar(progreso, costo)) return null
        return progreso.copy(
            monedas = progreso.monedas - costo,
            estadisticas = progreso.estadisticas.copy(
                monedasGastadas = progreso.estadisticas.monedasGastadas + costo,
                pistasUsadas = progreso.estadisticas.pistasUsadas + 1,
            ),
        )
    }

    fun acreditar(progreso: Progreso, cantidad: Int): Progreso =
        if (cantidad <= 0) progreso else progreso.copy(
            monedas = progreso.monedas + cantidad,
            estadisticas = progreso.estadisticas.copy(
                monedasGanadas = progreso.estadisticas.monedasGanadas + cantidad,
            ),
        )
}
