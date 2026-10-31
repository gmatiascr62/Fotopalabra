package com.fotopalabra.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.fotopalabra.pistas.TipoPista
import com.fotopalabra.progreso.Progreso
import com.fotopalabra.recursos.CargadorImagenes
import com.fotopalabra.ui.PartidaUi
import com.fotopalabra.ui.componentes.BarraSuperior
import com.fotopalabra.ui.componentes.BotonGrande
import com.fotopalabra.ui.componentes.CasilleroLetra
import com.fotopalabra.ui.componentes.EstiloCasillero
import com.fotopalabra.ui.componentes.Etiqueta
import com.fotopalabra.ui.componentes.FichaLetra
import com.fotopalabra.ui.componentes.Fondo
import com.fotopalabra.ui.componentes.GrupoImagenes
import com.fotopalabra.ui.componentes.IconoMoneda
import com.fotopalabra.ui.tema.Colores
import kotlinx.coroutines.delay

/** Acciones que la pantalla de juego puede pedir al ViewModel. */
class AccionesJuego(
    val tocarFicha: (Int) -> Unit,
    val tocarCasillero: (Int) -> Unit,
    val borrar: () -> Unit,
    val limpiar: () -> Unit,
    val usarPista: (TipoPista) -> Unit,
    val pistaDisponible: (TipoPista) -> Boolean,
    val descartarAviso: (com.fotopalabra.ui.Aviso) -> Unit,
    val volver: () -> Unit,
)

@Composable
fun PantallaJuego(
    partida: PartidaUi,
    progreso: Progreso,
    cargador: CargadorImagenes,
    acciones: AccionesJuego,
) {
    val haptic = LocalHapticFeedback.current
    val vibrar = progreso.ajustes.vibracion
    var mostrarPistas by rememberSaveable { mutableStateOf(false) }

    // Sacudida de los casilleros ante una respuesta incorrecta.
    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(partida.fallos) {
        if (partida.fallos > 0) {
            if (vibrar) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            for (x in listOf(18f, -16f, 12f, -8f, 4f, 0f)) sacudida.animateTo(x, androidx.compose.animation.core.tween(45))
        }
    }
    LaunchedEffect(partida.resuelto) {
        if (partida.resuelto && vibrar) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    val aviso = partida.aviso
    LaunchedEffect(aviso?.id) {
        if (aviso != null) {
            delay(2200)
            acciones.descartarAviso(aviso)
        }
    }

    Fondo {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val apaisado = maxWidth > maxHeight && maxWidth >= 560.dp
            Column(Modifier.fillMaxSize()) {
                BarraSuperior(
                    titulo = "Nivel ${partida.nivel.numero}",
                    monedas = progreso.monedas,
                    onVolver = acciones.volver,
                    extra = { Etiqueta(partida.nivel.dificultad.etiqueta, Colores.de(partida.nivel.dificultad)) },
                )
                if (apaisado) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        GrupoImagenes(
                            partida.nivel.imagenes,
                            cargador,
                            Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp),
                        )
                        Column(
                            Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ZonaRespuesta(partida, sacudida.value, acciones, { mostrarPistas = true })
                        }
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        GrupoImagenes(
                            partida.nivel.imagenes,
                            cargador,
                            Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                        )
                        ZonaRespuesta(partida, sacudida.value, acciones, { mostrarPistas = true })
                    }
                }
            }
            // Aviso flotante (error o costo de pista).
            AnimatedVisibility(
                visible = aviso != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
            ) {
                val a = aviso
                if (a != null) {
                    Text(
                        a.texto,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (a.esError) Colores.Rosa else Colores.VioletaOscuro)
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        color = Colores.Blanco,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (mostrarPistas) {
        DialogoPistas(
            monedas = progreso.monedas,
            disponible = acciones.pistaDisponible,
            onElegir = { tipo -> mostrarPistas = false; acciones.usarPista(tipo) },
            onCerrar = { mostrarPistas = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ZonaRespuesta(
    partida: PartidaUi,
    desplazamiento: Float,
    acciones: AccionesJuego,
    abrirPistas: () -> Unit,
) {
    val estado = partida.estado
    if (partida.pistas.textoVisible && partida.nivel.pista.isNotBlank()) {
        Text(
            "Pista: ${partida.nivel.pista}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Colores.VioletaOscuro.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            color = Colores.Amarillo,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }

    BoxWithConstraints(Modifier.fillMaxWidth().widthIn(max = 640.dp)) {
        val largo = estado.casilleros.size
        val porFila = if (largo <= 9) largo else (largo + 1) / 2
        val espacio = 6.dp
        val tamano = min(54.dp, (maxWidth - espacio * (porFila - 1)) / porFila)
        val incorrecto = estado.completo && !partida.resuelto && partida.fallos > 0
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .offset { IntOffset(desplazamiento.dp.roundToPx(), 0) },
            horizontalArrangement = Arrangement.spacedBy(espacio, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(espacio),
            maxItemsInEachRow = porFila,
        ) {
            estado.casilleros.forEachIndexed { i, c ->
                val estilo = when {
                    partida.resuelto -> EstiloCasillero.CORRECTO
                    c.revelado -> EstiloCasillero.REVELADO
                    c.fichaId == null -> EstiloCasillero.VACIO
                    incorrecto -> EstiloCasillero.INCORRECTO
                    else -> EstiloCasillero.LLENO
                }
                CasilleroLetra(estado.letraEn(i), estilo, tamano) { acciones.tocarCasillero(i) }
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth().widthIn(max = 640.dp)) {
        val total = estado.fichas.size
        val porFila = (total + 1) / 2
        val espacio = 8.dp
        val tamano = min(58.dp, (maxWidth - espacio * (porFila - 1)) / porFila)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(espacio, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(espacio),
            maxItemsInEachRow = porFila,
        ) {
            estado.fichas.forEach { f ->
                if (f.eliminada) {
                    Spacer(Modifier.width(tamano).height(tamano).alpha(0f))
                } else {
                    FichaLetra(f.letra, estado.estaDisponible(f), tamano) { acciones.tocarFicha(f.id) }
                }
            }
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .padding(top = 10.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tocar: borra la última letra. Mantener presionado: borra todas.
        BotonGrande(
            texto = "BORRAR",
            onClick = acciones.borrar,
            onLongClick = acciones.limpiar,
            modifier = Modifier.weight(1f),
            color = Colores.Rosa,
            sombra = Colores.RosaSombra,
            colorTexto = Colores.Blanco,
            alto = 54.dp,
            tamanoTexto = 18,
        )
        BotonGrande(
            texto = "PISTAS",
            onClick = abrirPistas,
            modifier = Modifier.weight(1f),
            color = Colores.Verde,
            sombra = Colores.VerdeSombra,
            colorTexto = Colores.Blanco,
            alto = 54.dp,
            tamanoTexto = 18,
        )
    }
}

@Composable
private fun DialogoPistas(
    monedas: Int,
    disponible: (TipoPista) -> Boolean,
    onElegir: (TipoPista) -> Unit,
    onCerrar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pistas", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black)
                IconoMoneda(20.dp)
                Spacer(Modifier.width(4.dp))
                Text("$monedas", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TipoPista.entries.forEach { tipo ->
                    OpcionPista(tipo, disponible(tipo), monedas >= tipo.costo) { onElegir(tipo) }
                }
            }
        },
    )
}

@Composable
private fun OpcionPista(tipo: TipoPista, disponible: Boolean, alcanza: Boolean, onClick: () -> Unit) {
    val activa = disponible && alcanza
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (activa) Colores.Violeta.copy(alpha = 0.1f) else Colores.Gris.copy(alpha = 0.2f))
            .clickable(enabled = activa, onClick = onClick)
            .padding(12.dp)
            .alpha(if (activa) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(tipo.titulo, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Colores.Texto)
            Text(
                when {
                    !disponible -> "Ya usada en este nivel"
                    !alcanza -> "No te alcanzan las monedas"
                    else -> tipo.descripcion
                },
                fontSize = 13.sp,
                color = Colores.Texto,
            )
        }
        Spacer(Modifier.width(8.dp))
        IconoMoneda(18.dp)
        Spacer(Modifier.width(4.dp))
        Text("${tipo.costo}", fontWeight = FontWeight.Black, color = Colores.Texto)
    }
}

