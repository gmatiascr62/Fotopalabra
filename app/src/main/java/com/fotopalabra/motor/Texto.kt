package com.fotopalabra.motor

import java.text.Normalizer

/**
 * Utilidades de texto para el español.
 *
 * Las respuestas se comparan en mayúsculas y sin tildes (Á→A, Ü→U), pero la Ñ se
 * conserva porque es una letra propia del alfabeto español.
 */
object Texto {

    /** Letras válidas en los casilleros y en las fichas. */
    const val ALFABETO = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ"

    fun normalizar(texto: String): String {
        val sb = StringBuilder()
        for (c in texto.trim().uppercase()) {
            when (c) {
                'Ñ' -> sb.append('Ñ')
                ' ', '-', '\'' -> Unit // se ignoran separadores
                else -> {
                    val base = Normalizer.normalize(c.toString(), Normalizer.Form.NFD)
                        .filter { it.code < 0x300 || it.code > 0x36F }
                    sb.append(base)
                }
            }
        }
        return sb.toString()
    }

    fun esValida(solucion: String): Boolean =
        solucion.isNotEmpty() && solucion.all { it in ALFABETO }
}
