package com.fotopalabra.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fotopalabra.ui.pantallas.AccionesJuego
import com.fotopalabra.ui.pantallas.PantallaAjustes
import com.fotopalabra.ui.pantallas.PantallaInicio
import com.fotopalabra.ui.pantallas.PantallaJuego
import com.fotopalabra.ui.pantallas.PantallaNiveles
import com.fotopalabra.ui.pantallas.PantallaVictoria
import com.fotopalabra.ui.tema.TemaFotopalabra

class MainActivity : ComponentActivity() {

    private val viewModel: JuegoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaFotopalabra { AppFotopalabra(viewModel) }
        }
    }
}

@Composable
fun AppFotopalabra(vm: JuegoViewModel) {
    val progreso by vm.progreso.collectAsStateWithLifecycle()
    val catalogo = vm.catalogo
    val pantalla = vm.pantalla

    BackHandler(enabled = vm.puedeVolver) {
        if (pantalla is Pantalla.Victoria) vm.irAlInicio() else vm.volver()
    }

    AnimatedContent(
        targetState = pantalla,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it::class },
        label = "pantallas",
    ) { destino ->
        when (destino) {
            Pantalla.Inicio -> PantallaInicio(
                catalogo = catalogo,
                progreso = progreso,
                onJugar = vm::jugar,
                onNiveles = { vm.irA(Pantalla.Niveles) },
                onAjustes = { vm.irA(Pantalla.Ajustes) },
            )

            Pantalla.Niveles -> catalogo?.let { cat ->
                PantallaNiveles(
                    catalogo = cat,
                    progreso = progreso,
                    onElegir = vm::abrirNivel,
                    onVolver = vm::volver,
                )
            }

            Pantalla.Ajustes -> PantallaAjustes(
                catalogo = catalogo,
                progreso = progreso,
                cargador = vm.imagenes,
                onCambiarAjustes = vm::cambiarAjustes,
                onReiniciar = vm::reiniciarProgreso,
                onVolver = vm::volver,
            )

            is Pantalla.Juego -> vm.partida?.let { partida ->
                PantallaJuego(
                    partida = partida,
                    progreso = progreso,
                    cargador = vm.imagenes,
                    acciones = AccionesJuego(
                        tocarFicha = vm::tocarFicha,
                        tocarCasillero = vm::tocarCasillero,
                        borrar = vm::borrar,
                        limpiar = vm::limpiar,
                        usarPista = vm::usarPista,
                        pistaDisponible = vm::pistaDisponible,
                        descartarAviso = vm::descartarAviso,
                        volver = vm::volver,
                    ),
                )
            }

            is Pantalla.Victoria -> catalogo?.porId(destino.nivelId)?.let { nivel ->
                PantallaVictoria(
                    nivel = nivel,
                    monedasGanadas = destino.monedasGanadas,
                    primeraVez = destino.primeraVez,
                    haySiguiente = catalogo.siguiente(nivel) != null,
                    avanceAutomatico = progreso.ajustes.avanceAutomatico,
                    onSiguiente = vm::siguienteNivel,
                    onMenu = vm::irAlInicio,
                )
            }
        }
    }
}
