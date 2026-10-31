package com.fotopalabra.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.fotopalabra.niveles.Imagen
import com.fotopalabra.recursos.CargadorImagenes
import com.fotopalabra.ui.tema.Colores

/**
 * Muestra las imágenes del nivel lo más grandes posible dentro del espacio disponible,
 * en una o dos filas, separadas por un signo "+".
 */
@Composable
fun GrupoImagenes(
    imagenes: List<Imagen>,
    cargador: CargadorImagenes,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val n = imagenes.size.coerceAtLeast(1)
        val separador = if (n > 1) 26.dp else 0.dp
        // Se elige la distribución (1 o 2 filas) que deja las imágenes más grandes.
        val (filas, lado) = (1..minOf(2, n)).map { f ->
            val columnas = (n + f - 1) / f
            val ancho = (maxWidth - separador * (columnas - 1)) / columnas
            val alto = (maxHeight - 12.dp * (f - 1)) / f
            f to min(min(ancho, alto), 340.dp)
        }.maxBy { it.second.value }
        val columnas = (n + filas - 1) / filas

        if (imagenes.size == 1) {
            // Una sola imagen: se muestra con su proporción real (puede ser apaisada).
            ImagenNivel(
                imagenes.first(),
                cargador,
                Modifier.sizeIn(maxWidth = min(maxWidth, 560.dp), maxHeight = min(maxHeight, 560.dp)),
                respetarProporcion = true,
            )
            return@BoxWithConstraints
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            imagenes.chunked(columnas).forEach { grupo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    grupo.forEachIndexed { i, imagen ->
                        if (i > 0) Signo(separador)
                        ImagenNivel(imagen, cargador, Modifier.size(lado))
                    }
                }
            }
        }
    }
}

@Composable
private fun Signo(ancho: Dp) {
    Text(
        "+",
        modifier = Modifier.width(ancho),
        color = Colores.Amarillo,
        fontSize = 30.sp,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
    )
}

/** Una ilustración del nivel. Si el archivo todavía no existe, muestra el marcador temporal. */
@Composable
fun ImagenNivel(
    imagen: Imagen,
    cargador: CargadorImagenes,
    modifier: Modifier = Modifier,
    respetarProporcion: Boolean = false,
) {
    val ladoPx = with(LocalDensity.current) { 560.dp.roundToPx() }
    val bitmap by produceState<ImageBitmap?>(null, imagen.archivo) {
        value = cargador.cargar(imagen.archivo, ladoPx)
    }
    val forma = RoundedCornerShape(20.dp)
    val proporcion = bitmap?.takeIf { respetarProporcion }?.let { it.width.toFloat() / it.height } ?: 1f
    Box(
        modifier
            .aspectRatio(proporcion)
            .shadow(8.dp, forma)
            .clip(forma)
            .background(Colores.Blanco),
        contentAlignment = Alignment.Center,
    ) {
        val b = bitmap
        when {
            b != null -> Image(
                bitmap = b,
                contentDescription = imagen.descripcion.ifBlank { null },
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            !cargador.existe(imagen.archivo) -> MarcadorTemporal(imagen)
        }
    }
}

/**
 * Marcador de desarrollo para imágenes que el usuario todavía no subió.
 * Muestra claramente "IMAGEN PENDIENTE", el nombre del archivo esperado y qué debe dibujarse.
 */
@Composable
fun MarcadorTemporal(imagen: Imagen) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF1EEFF)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val paso = 22.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawLine(Color(0xFFE2DCFF), Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 6.dp.toPx())
                x += paso
            }
            val margen = 8.dp.toPx()
            drawRoundRect(
                color = Colores.Violeta,
                topLeft = Offset(margen, margen),
                size = androidx.compose.ui.geometry.Size(size.width - margen * 2, size.height - margen * 2),
                cornerRadius = CornerRadius(14.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
            )
        }
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "IMAGEN PENDIENTE",
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Colores.Rosa)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = Colores.Blanco,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                imagen.archivo,
                color = Colores.VioletaOscuro,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (imagen.descripcion.isNotBlank()) {
                Text(
                    imagen.descripcion,
                    color = Colores.Texto,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
