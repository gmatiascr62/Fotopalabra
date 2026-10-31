package com.fotopalabra

import com.fotopalabra.motor.MotorJuego
import com.fotopalabra.niveles.CargadorNiveles
import com.fotopalabra.niveles.Dificultad
import com.fotopalabra.pistas.PistasNivel
import com.fotopalabra.progreso.Almacen
import com.fotopalabra.progreso.ReglasProgreso
import com.fotopalabra.progreso.RepositorioProgreso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NivelesYProgresoTest {

    /** Valida el archivo real de niveles: si alguien lo rompe, falla la compilación en GitHub Actions. */
    @Test
    fun losArchivosDeNivelesSonValidos() {
        val carpeta = File("src/main/assets/niveles")
        val archivos = carpeta.listFiles { f -> f.extension == "json" }!!.map { it.name to it.readText() }
        val catalogo = CargadorNiveles.cargar(archivos)
        assertEquals(emptyList<String>(), catalogo.errores)
        assertTrue(catalogo.total >= 20)
        // Toda imagen referenciada debe tener un nombre único.
        assertEquals(catalogo.archivosImagen.size, catalogo.niveles.sumOf { it.imagenes.size })
        catalogo.niveles.forEach { MotorJuego.nuevaPartida(it.solucion, it.id.toLong(), it.fichas) }
        assertEquals(Dificultad.FACIL, catalogo.niveles.first().dificultad)
    }

    @Test
    fun admiteImagenesComoTextoYDetectaErrores() {
        val json = """
            { "niveles": [
              { "id": 2, "respuesta": "Sol dado", "dificultad": "intermedio", "imagenes": ["a.png", "b.png"] },
              { "id": 1, "respuesta": "gato", "imagenes": [{ "archivo": "g.webp" }], },
              { "id": 1, "respuesta": "otro", "imagenes": ["x.png"] },
              { "id": 3, "respuesta": "123", "imagenes": ["x.png"] },
              { "id": 4, "respuesta": "casa", "imagenes": [] }
            ] }
        """.trimIndent()
        val catalogo = CargadorNiveles.cargar(listOf("n.json" to json, "roto.json" to "{"))
        assertEquals(listOf(1, 2), catalogo.niveles.map { it.id })
        assertEquals(listOf(1, 2), catalogo.niveles.map { it.numero })
        assertEquals("SOLDADO", catalogo.niveles[1].solucion)
        assertEquals(2, catalogo.niveles[1].imagenes.size)
        assertEquals(4, catalogo.errores.size)
    }

    private class Memoria : Almacen {
        var datos: String? = null
        override fun leer() = datos
        override fun escribir(datos: String) { this.datos = datos }
    }

    @Test
    fun guardaYRecuperaElProgreso() {
        val catalogo = CargadorNiveles.cargar(
            listOf("n.json" to """{"niveles":[{"id":1,"respuesta":"sol","imagenes":["a.png"]},{"id":2,"respuesta":"luna","imagenes":["b.png"]}]}""")
        )
        val almacen = Memoria()
        val repo = RepositorioProgreso(almacen)
        val n1 = catalogo.niveles[0]
        val n2 = catalogo.niveles[1]
        assertFalse(ReglasProgreso.estaDesbloqueado(repo.actual, n2))

        repo.actualizar { ReglasProgreso.guardarPistas(it, n1.id, PistasNivel(letrasEliminadas = true)) }
        var ganadas = 0
        repo.actualizar { ReglasProgreso.completar(it, n1).also { r -> ganadas = r.monedasGanadas }.progreso }
        assertEquals(Dificultad.FACIL.recompensa, ganadas)

        val recargado = RepositorioProgreso(almacen).actual
        assertTrue(ReglasProgreso.estaDesbloqueado(recargado, n2))
        assertEquals(n2, ReglasProgreso.nivelActual(recargado, catalogo))
        assertEquals(100 + ganadas, recargado.monedas)
        assertTrue(recargado.pistas.isEmpty())

        // Modo prueba: todo desbloqueado sin perder el progreso.
        val bloqueado = RepositorioProgreso(Memoria()).actual
        assertFalse(ReglasProgreso.estaDesbloqueado(bloqueado, n2))
        val prueba = bloqueado.copy(ajustes = bloqueado.ajustes.copy(desbloquearTodo = true))
        assertTrue(ReglasProgreso.estaDesbloqueado(prueba, n2))

        // Repetir un nivel no vuelve a dar monedas.
        assertEquals(0, ReglasProgreso.completar(recargado, n1).monedasGanadas)
    }
}
