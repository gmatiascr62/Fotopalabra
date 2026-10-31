package com.fotopalabra.ui.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fotopalabra.ui.tema.Colores

enum class EstiloCasillero { VACIO, LLENO, REVELADO, CORRECTO, INCORRECTO }

/** Casillero de la respuesta. */
@Composable
fun CasilleroLetra(
    letra: Char?,
    estilo: EstiloCasillero,
    tamano: Dp,
    onClick: () -> Unit,
) {
    val forma = RoundedCornerShape(tamano * 0.22f)
    val (fondo, texto) = when (estilo) {
        EstiloCasillero.VACIO -> Color.White.copy(alpha = 0.18f) to Colores.Blanco
        EstiloCasillero.LLENO -> Colores.Blanco to Colores.Texto
        EstiloCasillero.REVELADO -> Colores.Celeste to Colores.Blanco
        EstiloCasillero.CORRECTO -> Colores.Verde to Colores.Blanco
        EstiloCasillero.INCORRECTO -> Colores.Rosa to Colores.Blanco
    }
    val escala by animateFloatAsState(if (letra != null) 1f else 0.92f, spring(dampingRatio = 0.45f), label = "casillero")
    Box(
        Modifier
            .size(tamano)
            .scale(escala)
            .clip(forma)
            .background(fondo)
            .border(2.dp, Color.White.copy(alpha = if (estilo == EstiloCasillero.VACIO) 0.6f else 0f), forma)
            .clickable(enabled = letra != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (letra != null) {
            Text(letra.toString(), color = texto, fontSize = (tamano.value * 0.5f).sp, fontWeight = FontWeight.Black)
        }
    }
}

/** Ficha de letra disponible para tocar. Si ya se usó, queda su hueco. */
@Composable
fun FichaLetra(
    letra: Char,
    visible: Boolean,
    tamano: Dp,
    onClick: () -> Unit,
) {
    val forma = RoundedCornerShape(tamano * 0.24f)
    val escala by animateFloatAsState(if (visible) 1f else 0f, spring(dampingRatio = 0.55f), label = "ficha")
    Box(Modifier.size(tamano)) {
        // Hueco que queda cuando la ficha está colocada.
        Box(
            Modifier
                .size(tamano)
                .clip(forma)
                .background(Colores.VioletaOscuro.copy(alpha = 0.25f)),
        )
        if (escala > 0.01f) {
            Box(
                Modifier
                    .size(tamano)
                    .scale(escala)
                    .clip(forma)
                    .background(Colores.AmarilloSombra)
                    .clickable(enabled = visible, onClick = onClick),
            ) {
                Box(
                    Modifier
                        .size(tamano, tamano - 4.dp)
                        .offset(y = 0.dp)
                        .clip(forma)
                        .background(Colores.Amarillo),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        letra.toString(),
                        color = Colores.Texto,
                        fontSize = (tamano.value * 0.48f).sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}
