package com.fotopalabra.ui.pantallas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fotopalabra.niveles.CatalogoNiveles
import com.fotopalabra.progreso.Progreso
import com.fotopalabra.progreso.ReglasProgreso
import com.fotopalabra.ui.componentes.BotonGrande
import com.fotopalabra.ui.componentes.ContadorMonedas
import com.fotopalabra.ui.componentes.Fondo
import com.fotopalabra.ui.tema.Colores

@Composable
fun PantallaInicio(
    catalogo: CatalogoNiveles?,
    progreso: Progreso,
    onJugar: () -> Unit,
    onNiveles: () -> Unit,
    onAjustes: () -> Unit,
) {
    Fondo {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Logotipo()
            Spacer(Modifier.height(8.dp))
            Text(
                "Adiviná la palabra escondida en las imágenes",
                color = Colores.Blanco.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))

            if (catalogo == null) {
                CircularProgressIndicator(color = Colores.Amarillo)
                return@Column
            }

            val actual = ReglasProgreso.nivelActual(progreso, catalogo)
            val terminado = ReglasProgreso.todoCompletado(progreso, catalogo)
            Column(
                Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Colores.VioletaOscuro.copy(alpha = 0.35f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    when {
                        catalogo.total == 0 -> "No hay niveles cargados"
                        terminado -> "¡Completaste los ${catalogo.total} niveles!"
                        actual != null -> "Nivel ${actual.numero}"
                        else -> ""
                    },
                    color = Colores.Amarillo,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${progreso.completados.count { catalogo.porId(it) != null }} de ${catalogo.total} completados",
                    color = Colores.Blanco,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(20.dp))
                BotonGrande(
                    texto = if (terminado) "VER NIVELES" else "JUGAR",
                    onClick = onJugar,
                    modifier = Modifier.fillMaxWidth(),
                    alto = 68.dp,
                    tamanoTexto = 28,
                    habilitado = catalogo.total > 0,
                )
                Spacer(Modifier.height(14.dp))
                BotonGrande(
                    texto = "NIVELES",
                    onClick = onNiveles,
                    modifier = Modifier.fillMaxWidth(),
                    color = Colores.Celeste,
                    sombra = Colores.Violeta,
                    colorTexto = Colores.Blanco,
                    alto = 52.dp,
                    tamanoTexto = 18,
                )
            }
        }

        // La barra va al final para quedar por encima del contenido desplazable
        // (si no, el contenido tapa el engranaje y no recibe los toques).
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ContadorMonedas(progreso.monedas)
            IconButton(
                onClick = onAjustes,
                modifier = Modifier.clip(CircleShape).background(Colores.VioletaOscuro.copy(alpha = 0.4f)),
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Configuración", tint = Colores.Blanco)
            }
        }
    }
}

/** Nombre del juego armado con fichas, con un leve balanceo. */
@Composable
private fun Logotipo() {
    val transicion = rememberInfiniteTransition(label = "logo")
    val balanceo by transicion.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "balanceo",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            "FOTO".forEachIndexed { i, letra -> FichaLogo(letra, balanceo * if (i % 2 == 0) 4f else -4f) }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            "PALABRA".forEachIndexed { i, letra -> FichaLogo(letra, balanceo * if (i % 2 == 0) -3f else 3f, pequena = true) }
        }
    }
}

@Composable
private fun FichaLogo(letra: Char, giro: Float, pequena: Boolean = false) {
    val lado = if (pequena) 40.dp else 58.dp
    Box(
        Modifier
            .rotate(giro)
            .clip(RoundedCornerShape(12.dp))
            .background(if (pequena) Colores.Blanco else Colores.Amarillo),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.padding(horizontal = 4.dp).height(lado).widthIn(min = lado * 0.8f), contentAlignment = Alignment.Center) {
            Text(
                letra.toString(),
                color = Colores.Texto,
                fontSize = (lado.value * 0.6f).sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
