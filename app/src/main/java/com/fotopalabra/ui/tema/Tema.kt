package com.fotopalabra.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fotopalabra.niveles.Dificultad

object Colores {
    val VioletaOscuro = Color(0xFF2B1F99)
    val Violeta = Color(0xFF4A3AFF)
    val Lila = Color(0xFF8E3BFF)
    val Amarillo = Color(0xFFFFC93C)
    val AmarilloSombra = Color(0xFFE0A01E)
    val Verde = Color(0xFF3DDC97)
    val VerdeSombra = Color(0xFF22A873)
    val Rosa = Color(0xFFFF5C7A)
    val RosaSombra = Color(0xFFD13A57)
    val Naranja = Color(0xFFFFB547)
    val Celeste = Color(0xFF4FC3F7)
    val Blanco = Color.White
    val Texto = Color(0xFF241A6B)
    val Gris = Color(0xFFB9B4E0)

    val fondo = Brush.verticalGradient(listOf(Violeta, Lila))

    fun de(dificultad: Dificultad): Color = when (dificultad) {
        Dificultad.FACIL -> Verde
        Dificultad.INTERMEDIO -> Naranja
        Dificultad.DIFICIL -> Rosa
    }
}

private val tipografia = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
)

@Composable
fun TemaFotopalabra(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Colores.Violeta,
            onPrimary = Colores.Blanco,
            secondary = Colores.Amarillo,
            onSecondary = Colores.Texto,
            surface = Colores.Blanco,
            onSurface = Colores.Texto,
            background = Colores.Violeta,
            error = Colores.Rosa,
        ),
        typography = tipografia,
        content = content,
    )
}
