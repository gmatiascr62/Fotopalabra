package com.fotopalabra

import com.fotopalabra.motor.MotorJuego
import com.fotopalabra.motor.Resultado
import com.fotopalabra.motor.Texto
import com.fotopalabra.pistas.SistemaPistas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MotorJuegoTest {

    private fun escribir(solucion: String, semilla: Long = 1): com.fotopalabra.motor.EstadoPartida {
        var e = MotorJuego.nuevaPartida(solucion, semilla)
        for (letra in solucion) {
            val ficha = e.fichas.first { it.letra == letra && e.estaDisponible(it) }
            e = MotorJuego.colocar(e, ficha.id)
        }
        return e
    }

    @Test
    fun normalizaTildesPeroConservaEnie() {
        assertEquals("ARBOL", Texto.normalizar("Árbol"))
        assertEquals("TELARAÑA", Texto.normalizar("telaraña"))
        assertEquals("PINGUINO", Texto.normalizar("pingüino"))
        assertEquals("ARCOIRIS", Texto.normalizar("Arco iris"))
    }

    @Test
    fun lasFichasContienenLaSolucionYSonDeterministas() {
        val a = MotorJuego.nuevaPartida("TELARAÑA", 10)
        val b = MotorJuego.nuevaPartida("TELARAÑA", 10)
        assertEquals(a, b)
        assertEquals(12, a.fichas.size)
        assertEquals("AAAELRTÑ", a.fichas.filter { it.deLaSolucion }.map { it.letra }.sorted().joinToString(""))
    }

    @Test
    fun aciertaYFalla() {
        val ok = escribir("GATO")
        assertEquals(Resultado.CORRECTO, MotorJuego.evaluar(ok))

        var mal = MotorJuego.nuevaPartida("GATO", 3)
        mal.fichas.take(4).forEach { mal = MotorJuego.colocar(mal, it.id) }
        if (mal.palabraActual != "GATO") assertEquals(Resultado.INCORRECTO, MotorJuego.evaluar(mal))

        val incompleto = MotorJuego.borrarUltima(ok)
        assertEquals(Resultado.INCOMPLETO, MotorJuego.evaluar(incompleto))
        assertEquals("GAT_", incompleto.palabraActual)
    }

    @Test
    fun noSeUsaDosVecesLaMismaFicha() {
        var e = MotorJuego.nuevaPartida("SOL", 1)
        val id = e.fichas.first().id
        e = MotorJuego.colocar(e, id)
        e = MotorJuego.colocar(e, id)
        assertEquals(1, e.casilleros.count { it.fichaId != null })
    }

    @Test
    fun revelarLetraCompletaHastaResolver() {
        repeat(30) { semilla ->
            var e = MotorJuego.nuevaPartida("BALONCESTO", semilla.toLong())
            // Llenar con letras cualquiera para forzar movimientos.
            e.fichas.forEach { e = MotorJuego.colocar(e, it.id) }
            val r = Random(semilla)
            repeat(e.solucion.length) { e = SistemaPistas.revelarLetra(e, r)!!.first }
            assertEquals(Resultado.CORRECTO, MotorJuego.evaluar(e))
            assertTrue(e.casilleros.all { it.revelado })
        }
    }

    @Test
    fun eliminarLetrasQuitaSoloElRellenoYPermiteResolver() {
        var e = MotorJuego.nuevaPartida("CAMALEON", 5)
        e.fichas.forEach { e = MotorJuego.colocar(e, it.id) }
        e = SistemaPistas.eliminarLetras(e)
        assertTrue(e.fichas.filter { it.eliminada }.none { it.deLaSolucion })
        assertEquals(8, e.fichas.count { !it.eliminada })
        // Con las letras restantes se puede escribir la respuesta.
        e = MotorJuego.limpiar(e)
        for (letra in e.solucion) {
            e = MotorJuego.colocar(e, e.fichas.first { it.letra == letra && e.estaDisponible(it) }.id)
        }
        assertEquals(Resultado.CORRECTO, MotorJuego.evaluar(e))
    }

    @Test
    fun lasLetrasReveladasNoSeBorran() {
        var e = MotorJuego.nuevaPartida("PIRATA", 2)
        e = SistemaPistas.revelarEn(e, 5)
        e = MotorJuego.limpiar(e)
        e = MotorJuego.borrarUltima(e)
        assertEquals('A', e.letraEn(5))
        assertFalse(SistemaPistas.revelarEn(e, 5) != e)
    }
}
