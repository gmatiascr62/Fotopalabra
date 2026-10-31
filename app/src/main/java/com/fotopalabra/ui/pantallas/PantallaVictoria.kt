package com.fotopalabra.ui.pantallas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fotopalabra.niveles.Nivel
import com.fotopalabra.ui.componentes.BotonGrande
import com.fotopalabra.ui.componentes.Confeti
import com.fotopalabra.ui.componentes.Fondo
import com.fotopalabra.ui.componentes.IconoMoneda
import com.fotopalabra.ui.tema.Colores
import kotlinx.coroutines.delay

private val FELICITACIONES = listOf("¡Excelente!", "¡Muy bien!", "¡Genial!", "¡Lo lograste!", "¡Bárbaro!", "¡Increíble!")

@Composable
fun PantallaVictoria(
    nivel: Nivel,
    monedasGanadas: Int,
    primeraVez: Boolean,
    haySiguiente: Boolean,
    avanceAutomatico: Boolean,
    onSiguiente: () -> Unit,
    onMenu: () -> Unit,
) {
    val titulo = remember(nivel.id) { FELICITACIONES[nivel.id % FELICITACIONES.size] }
    val escala = remember { Animatable(0.3f) }
    LaunchedEffect(nivel.id) {
        escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    // Avance automático con cuenta regresiva.
    var restante by remember(nivel.id) { mutableIntStateOf(4) }
    if (avanceAutomatico && haySiguiente) {
        LaunchedEffect(nivel.id) {
            while (restante > 0) {
                delay(1000)
                restante--
            }
            onSiguiente()
        }
    }

    Fondo {
        Confeti(Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                titulo,
                modifier = Modifier.scale(escala.value),
                style = MaterialTheme.typography.displayLarge,
                color = Colores.Amarillo,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text("Nivel ${nivel.numero} superado", color = Colores.Blanco, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            // Palabra descubierta.
            BoxWithConstraints(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                val largo = nivel.solucion.length
                val espacio = if (largo > 10) 3.dp else 6.dp
                val tamano = tamanoQueEntra(maxWidth, largo, espacio, maximo = 46.dp)
                Row(
                    Modifier.fillMaxWidth().scale(escala.value),
                    horizontalArrangement = Arrangement.spacedBy(espacio, Alignment.CenterHorizontally),
                ) {
                    nivel.solucion.forEach { letra ->
                        Box(
                            Modifier
                                .size(tamano)
                                .clip(RoundedCornerShape(tamano * 0.22f))
                                .background(Colores.Verde),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                letra.toString(),
                                color = Colores.Blanco,
                                fontSize = (tamano.value * 0.52f).sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(nivel.respuesta, color = Colores.Blanco, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Colores.VioletaOscuro.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconoMoneda(28.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (monedasGanadas > 0) "+$monedasGanadas monedas" else if (primeraVez) "Sin monedas" else "Nivel repetido: sin monedas",
                    color = Colores.Blanco,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.height(32.dp))

            BotonGrande(
                texto = if (haySiguiente) "SIGUIENTE NIVEL" else "VER NIVELES",
                onClick = onSiguiente,
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                alto = 64.dp,
            )
            if (avanceAutomatico && haySiguiente && restante > 0) {
                Spacer(Modifier.height(8.dp))
                Text("Siguiente nivel en $restante…", color = Colores.Blanco.copy(alpha = 0.85f))
            }
            if (!haySiguiente) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "¡Completaste todos los niveles disponibles! Pronto habrá más.",
                    color = Colores.Blanco,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onMenu) {
                Text("Volver al menú", color = Colores.Blanco, fontWeight = FontWeight.Bold)
            }
        }
    }
}
