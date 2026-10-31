package com.fotopalabra.motor

import kotlin.random.Random

/**
 * Genera el conjunto de letras desordenadas de un nivel: todas las letras de la
 * solución más letras de relleno elegidas según la frecuencia del español.
 * Es determinista (misma semilla → mismas fichas), así el nivel se ve igual al volver.
 */
object GeneradorLetras {

    // Letras más frecuentes aparecen más veces para que el relleno parezca natural.
    private const val RELLENO = "AAAAEEEEOOOIIISSSRRRNNNLLLDDTTCCUUMMPPBGVFHJQYZÑ"

    fun cantidadFichas(largo: Int): Int = when {
        largo <= 5 -> 10
        largo <= 8 -> 12
        largo <= 10 -> 14
        else -> minOf(largo + 4, 20)
    }

    fun generar(solucion: String, semilla: Long, total: Int? = null): List<Ficha> {
        val random = Random(semilla)
        val cantidad = maxOf(total ?: cantidadFichas(solucion.length), solucion.length)
        val letras = mutableListOf<Pair<Char, Boolean>>()
        solucion.forEach { letras += it to true }
        while (letras.size < cantidad) {
            letras += RELLENO[random.nextInt(RELLENO.length)] to false
        }
        var mezcla = letras.shuffled(random)
        // Evitar que las primeras fichas muestren la respuesta en orden.
        var intentos = 0
        while (solucion.length > 1 && mezcla.take(solucion.length).map { it.first }.joinToString("") == solucion && intentos < 5) {
            mezcla = letras.shuffled(random)
            intentos++
        }
        return mezcla.mapIndexed { i, (letra, deSolucion) -> Ficha(i, letra, deSolucion) }
    }
}
