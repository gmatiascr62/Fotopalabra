package com.fotopalabra.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fotopalabra.datos.AlmacenPreferencias
import com.fotopalabra.datos.FuenteNivelesAssets
import com.fotopalabra.monedas.Billetera
import com.fotopalabra.motor.EstadoPartida
import com.fotopalabra.motor.MotorJuego
import com.fotopalabra.motor.Resultado
import com.fotopalabra.niveles.CatalogoNiveles
import com.fotopalabra.niveles.Nivel
import com.fotopalabra.pistas.PistasNivel
import com.fotopalabra.pistas.SistemaPistas
import com.fotopalabra.pistas.TipoPista
import com.fotopalabra.progreso.Ajustes
import com.fotopalabra.progreso.Progreso
import com.fotopalabra.progreso.ReglasProgreso
import com.fotopalabra.progreso.RepositorioProgreso
import com.fotopalabra.recursos.CargadorImagenes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Pantalla {
    data object Inicio : Pantalla
    data object Niveles : Pantalla
    data object Ajustes : Pantalla
    data class Juego(val nivelId: Int) : Pantalla
    data class Victoria(val nivelId: Int, val monedasGanadas: Int, val primeraVez: Boolean) : Pantalla
}

/** Mensaje breve que se muestra en la pantalla de juego. */
data class Aviso(val texto: String, val esError: Boolean = false, val id: Long = System.nanoTime())

data class PartidaUi(
    val nivel: Nivel,
    val estado: EstadoPartida,
    val pistas: PistasNivel,
    /** Se incrementa con cada respuesta incorrecta (dispara la animación de sacudida). */
    val fallos: Int = 0,
    val resuelto: Boolean = false,
    val aviso: Aviso? = null,
)

/**
 * Conecta la interfaz con el motor, las pistas, las monedas y el progreso.
 * La lógica de reglas vive en los paquetes `motor`, `pistas`, `monedas` y `progreso`.
 */
class JuegoViewModel(app: Application) : AndroidViewModel(app) {

    val imagenes = CargadorImagenes(app.assets)
    private val repositorio = RepositorioProgreso(AlmacenPreferencias(app))
    val progreso: StateFlow<Progreso> = repositorio.progreso

    var catalogo by mutableStateOf<CatalogoNiveles?>(null)
        private set

    private var pila by mutableStateOf<List<Pantalla>>(listOf(Pantalla.Inicio))
    val pantalla: Pantalla get() = pila.last()
    val puedeVolver: Boolean get() = pila.size > 1

    var partida by mutableStateOf<PartidaUi?>(null)
        private set

    private var victoriaPendiente: Job? = null

    init {
        viewModelScope.launch {
            catalogo = withContext(Dispatchers.IO) { FuenteNivelesAssets(app.assets).cargar() }
        }
    }

    // ---------- Navegación ----------

    fun irA(destino: Pantalla) {
        pila = pila + destino
    }

    fun volver() {
        if (pila.size > 1) pila = pila.dropLast(1)
        if (pantalla !is Pantalla.Juego) cancelarVictoria()
    }

    fun irAlInicio() {
        cancelarVictoria()
        pila = listOf(Pantalla.Inicio)
    }

    /** Botón JUGAR: continúa en el primer nivel pendiente. */
    fun jugar() {
        val cat = catalogo ?: return
        val nivel = ReglasProgreso.nivelActual(progreso.value, cat)
        if (nivel == null) irA(Pantalla.Niveles) else abrirNivel(nivel)
    }

    fun abrirNivel(nivel: Nivel) {
        if (!ReglasProgreso.estaDesbloqueado(progreso.value, nivel)) return
        cancelarVictoria()
        val pistas = progreso.value.pistasDe(nivel.id)
        val estado = SistemaPistas.restaurar(
            MotorJuego.nuevaPartida(nivel.solucion, semilla = nivel.id * 7919L, totalFichas = nivel.fichas),
            pistas,
        )
        partida = PartidaUi(nivel, estado, pistas)
        val destino = Pantalla.Juego(nivel.id)
        // Si venimos de la pantalla de victoria o de otro nivel, se reemplaza en lugar de apilar.
        pila = pila.filterNot { it is Pantalla.Juego || it is Pantalla.Victoria } + destino
    }

    fun siguienteNivel() {
        val cat = catalogo ?: return
        val actual = (pantalla as? Pantalla.Victoria)?.let { cat.porId(it.nivelId) }
        val siguiente = actual?.let { cat.siguiente(it) }
            ?.takeIf { ReglasProgreso.estaDesbloqueado(progreso.value, it) }
            ?: ReglasProgreso.nivelActual(progreso.value, cat)
        if (siguiente == null) {
            pila = listOf(Pantalla.Inicio, Pantalla.Niveles)
        } else {
            abrirNivel(siguiente)
        }
    }

    // ---------- Juego ----------

    fun tocarFicha(fichaId: Int) = modificar { MotorJuego.colocar(it, fichaId) }

    fun tocarCasillero(indice: Int) = modificar { MotorJuego.quitarDeCasillero(it, indice) }

    fun borrar() = modificar { MotorJuego.borrarUltima(it) }

    fun limpiar() = modificar { MotorJuego.limpiar(it) }

    private fun modificar(cambio: (EstadoPartida) -> EstadoPartida) {
        val p = partida ?: return
        if (p.resuelto) return
        val nuevo = cambio(p.estado)
        if (nuevo == p.estado) return
        partida = p.copy(estado = nuevo, aviso = null)
        comprobar()
    }

    private fun comprobar() {
        val p = partida ?: return
        when (MotorJuego.evaluar(p.estado)) {
            Resultado.INCOMPLETO -> Unit
            Resultado.INCORRECTO -> {
                repositorio.actualizar { ReglasProgreso.registrarIntentoFallido(it) }
                partida = p.copy(fallos = p.fallos + 1, aviso = Aviso("¡Casi! Probá otra combinación", esError = true))
            }
            Resultado.CORRECTO -> {
                partida = p.copy(resuelto = true, aviso = null)
                var resultado: com.fotopalabra.progreso.NivelCompletado? = null
                repositorio.actualizar { ReglasProgreso.completar(it, p.nivel).also { r -> resultado = r }.progreso }
                val r = resultado ?: return
                victoriaPendiente = viewModelScope.launch {
                    delay(700) // breve pausa para ver la palabra completa en verde
                    pila = pila.filterNot { it is Pantalla.Victoria } +
                        Pantalla.Victoria(p.nivel.id, r.monedasGanadas, r.primeraVez)
                }
            }
        }
    }

    private fun cancelarVictoria() {
        victoriaPendiente?.cancel()
        victoriaPendiente = null
    }

    // ---------- Pistas ----------

    fun pistaDisponible(tipo: TipoPista): Boolean {
        val p = partida ?: return false
        return !p.resuelto && SistemaPistas.disponible(tipo, p.estado, p.pistas)
    }

    fun usarPista(tipo: TipoPista) {
        val p = partida ?: return
        if (!pistaDisponible(tipo)) return
        if (!Billetera.puedePagar(progreso.value, tipo.costo)) {
            partida = p.copy(aviso = Aviso("Necesitás ${tipo.costo} monedas para esta pista", esError = true))
            return
        }
        val (estado, pistas) = when (tipo) {
            TipoPista.ELIMINAR_LETRAS -> SistemaPistas.eliminarLetras(p.estado) to p.pistas.copy(letrasEliminadas = true)
            TipoPista.REVELAR_LETRA -> {
                val (e, posicion) = SistemaPistas.revelarLetra(p.estado) ?: return
                e to p.pistas.copy(reveladas = p.pistas.reveladas + posicion)
            }
        }
        repositorio.actualizar { actual ->
            val cobrado = Billetera.cobrar(actual, tipo.costo) ?: return@actualizar actual
            ReglasProgreso.guardarPistas(cobrado, p.nivel.id, pistas)
        }
        partida = p.copy(estado = estado, pistas = pistas, aviso = Aviso("-${tipo.costo} monedas"))
        comprobar()
    }

    fun descartarAviso(aviso: Aviso) {
        val p = partida ?: return
        if (p.aviso?.id == aviso.id) partida = p.copy(aviso = null)
    }

    // ---------- Ajustes ----------

    fun cambiarAjustes(cambio: (Ajustes) -> Ajustes) =
        repositorio.actualizar { it.copy(ajustes = cambio(it.ajustes)) }

    fun reiniciarProgreso() {
        repositorio.reiniciar()
        partida = null
        irAlInicio()
    }
}
