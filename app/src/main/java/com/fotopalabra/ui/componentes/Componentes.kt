package com.fotopalabra.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fotopalabra.ui.tema.Colores

/** Fondo degradado común a todas las pantallas, respetando barras del sistema y recortes. */
@Composable
fun Fondo(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Colores.fondo)
            .safeDrawingPadding(),
        content = content,
    )
}

/** Moneda dibujada (sin imágenes externas). */
@Composable
fun IconoMoneda(tamano: Dp = 22.dp) {
    Canvas(Modifier.size(tamano)) {
        drawCircle(Colores.AmarilloSombra)
        drawCircle(Colores.Amarillo, radius = size.minDimension * 0.42f, center = center - Offset(0f, size.minDimension * 0.04f))
        drawCircle(Colores.AmarilloSombra, radius = size.minDimension * 0.24f, style = Stroke(width = size.minDimension * 0.08f))
    }
}

@Composable
fun ContadorMonedas(monedas: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Colores.VioletaOscuro.copy(alpha = 0.55f))
            .padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconoMoneda()
        Spacer(Modifier.width(6.dp))
        Text("$monedas", color = Colores.Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
    }
}

/** Botón "con relieve": se hunde al presionarlo. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BotonGrande(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Colores.Amarillo,
    sombra: Color = Colores.AmarilloSombra,
    colorTexto: Color = Colores.Texto,
    alto: Dp = 60.dp,
    habilitado: Boolean = true,
    tamanoTexto: Int = 22,
    onLongClick: (() -> Unit)? = null,
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val relieve = if (presionado) 1.dp else 5.dp
    val forma = RoundedCornerShape(18.dp)
    val fondo = if (habilitado) color else Colores.Gris
    val borde = if (habilitado) sombra else Colores.Gris.copy(alpha = 0.7f)
    Box(
        modifier
            .height(alto + 5.dp)
            .combinedClickable(
                interactionSource = interaccion,
                indication = null,
                enabled = habilitado,
                onLongClick = onLongClick,
                onClick = onClick,
            ),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(alto)
                .offset(y = 5.dp)
                .clip(forma)
                .background(borde),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(alto)
                .offset(y = 5.dp - relieve)
                .clip(forma)
                .background(fondo),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                texto,
                color = if (habilitado) colorTexto else Colores.Blanco,
                fontWeight = FontWeight.Black,
                fontSize = tamanoTexto.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/** Barra superior con botón de volver, título y monedas. */
@Composable
fun BarraSuperior(
    titulo: String,
    monedas: Int?,
    onVolver: (() -> Unit)?,
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onVolver != null) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Colores.VioletaOscuro.copy(alpha = 0.4f)),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Colores.Blanco)
            }
        }
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            color = Colores.Blanco,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        extra()
        if (monedas != null) ContadorMonedas(monedas)
    }
}

/** Etiqueta pequeña de color (por ejemplo, la dificultad). */
@Composable
fun Etiqueta(texto: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        texto.uppercase(),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        color = Colores.Texto,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
    )
}
