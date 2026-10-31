package com.fotopalabra.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.fotopalabra.ui.tema.Colores
import kotlin.math.sin
import kotlin.random.Random

private data class Papelito(
    val x: Float,
    val inicio: Float,
    val velocidad: Float,
    val oscilacion: Float,
    val giro: Float,
    val color: Color,
    val ancho: Float,
)

/** Lluvia de papelitos de colores dibujada en un Canvas (sin imágenes externas). */
@Composable
fun Confeti(modifier: Modifier = Modifier, cantidad: Int = 70) {
    val colores = listOf(Colores.Amarillo, Colores.Verde, Colores.Rosa, Colores.Celeste, Colores.Naranja, Color.White)
    val papelitos = remember {
        val r = Random(System.currentTimeMillis())
        List(cantidad) {
            Papelito(
                x = r.nextFloat(),
                inicio = r.nextFloat(),
                velocidad = 0.6f + r.nextFloat() * 0.8f,
                oscilacion = r.nextFloat() * 6.28f,
                giro = r.nextFloat() * 360f,
                color = colores[r.nextInt(colores.size)],
                ancho = 8f + r.nextFloat() * 10f,
            )
        }
    }
    val transicion = rememberInfiniteTransition(label = "confeti")
    val t by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "tiempo",
    )
    Canvas(modifier) {
        papelitos.forEach { p ->
            val progreso = (p.inicio + t * p.velocidad) % 1f
            val y = progreso * (size.height + 60f) - 30f
            val x = p.x * size.width + sin(progreso * 12f + p.oscilacion) * 24f
            rotate(p.giro + progreso * 720f, pivot = Offset(x, y)) {
                drawRect(p.color, topLeft = Offset(x - p.ancho / 2, y - p.ancho / 4), size = Size(p.ancho, p.ancho / 2))
            }
        }
    }
}
