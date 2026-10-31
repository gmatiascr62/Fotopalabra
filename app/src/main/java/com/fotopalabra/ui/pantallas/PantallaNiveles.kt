package com.fotopalabra.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fotopalabra.niveles.CatalogoNiveles
import com.fotopalabra.niveles.Nivel
import com.fotopalabra.progreso.Progreso
import com.fotopalabra.progreso.ReglasProgreso
import com.fotopalabra.ui.componentes.BarraSuperior
import com.fotopalabra.ui.componentes.Fondo
import com.fotopalabra.ui.tema.Colores

@Composable
fun PantallaNiveles(
    catalogo: CatalogoNiveles,
    progreso: Progreso,
    onElegir: (Nivel) -> Unit,
    onVolver: () -> Unit,
) {
    // Los niveles se agrupan en bloques consecutivos de la misma dificultad.
    val bloques = remember(catalogo) {
        val resultado = mutableListOf<MutableList<Nivel>>()
        catalogo.niveles.forEach { n ->
            if (resultado.lastOrNull()?.last()?.dificultad == n.dificultad) resultado.last() += n else resultado += mutableListOf(n)
        }
        resultado
    }
    val actual = ReglasProgreso.nivelActual(progreso, catalogo)

    Fondo {
        Column(Modifier.fillMaxSize()) {
            BarraSuperior("Niveles", progreso.monedas, onVolver)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(72.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                bloques.forEach { bloque ->
                    val dificultad = bloque.first().dificultad
                    item(span = { GridItemSpan(maxLineSpan) }, key = "titulo_${bloque.first().id}") {
                        Text(
                            "${dificultad.etiqueta} · niveles ${bloque.first().numero}–${bloque.last().numero}",
                            color = Colores.de(dificultad),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    items(bloque, key = { it.id }) { nivel ->
                        CeldaNivel(
                            nivel = nivel,
                            desbloqueado = ReglasProgreso.estaDesbloqueado(progreso, nivel),
                            completado = ReglasProgreso.estaCompletado(progreso, nivel),
                            esActual = nivel.id == actual?.id,
                            onClick = { onElegir(nivel) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CeldaNivel(
    nivel: Nivel,
    desbloqueado: Boolean,
    completado: Boolean,
    esActual: Boolean,
    onClick: () -> Unit,
) {
    val forma = RoundedCornerShape(18.dp)
    val fondo = when {
        completado -> Colores.Verde
        esActual -> Colores.Amarillo
        desbloqueado -> Colores.Blanco
        else -> Colores.VioletaOscuro.copy(alpha = 0.45f)
    }
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(forma)
            .background(fondo)
            .border(3.dp, if (desbloqueado) Colores.de(nivel.dificultad) else Colores.Gris.copy(alpha = 0.3f), forma)
            .clickable(enabled = desbloqueado, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (!desbloqueado) {
            Icon(Icons.Filled.Lock, contentDescription = "Bloqueado", tint = Colores.Gris, modifier = Modifier.size(26.dp))
        } else {
            Text(
                "${nivel.numero}",
                color = if (completado) Colores.Blanco else Colores.Texto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
            )
            if (completado) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Completado",
                    tint = Colores.Blanco,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).size(16.dp),
                )
            }
        }
    }
}
