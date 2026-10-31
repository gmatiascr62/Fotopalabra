package com.fotopalabra.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fotopalabra.niveles.CatalogoNiveles
import com.fotopalabra.progreso.Ajustes
import com.fotopalabra.progreso.Progreso
import com.fotopalabra.recursos.CargadorImagenes
import com.fotopalabra.ui.componentes.BarraSuperior
import com.fotopalabra.ui.componentes.BotonGrande
import com.fotopalabra.ui.componentes.Fondo
import com.fotopalabra.ui.tema.Colores

@Composable
fun PantallaAjustes(
    catalogo: CatalogoNiveles?,
    progreso: Progreso,
    cargador: CargadorImagenes,
    onCambiarAjustes: ((Ajustes) -> Ajustes) -> Unit,
    onReiniciar: () -> Unit,
    onVolver: () -> Unit,
) {
    var confirmarReinicio by rememberSaveable { mutableStateOf(false) }
    var verPendientes by rememberSaveable { mutableStateOf(false) }
    val contexto = LocalContext.current
    val version = remember {
        runCatching { contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName }.getOrNull() ?: "?"
    }

    Fondo {
        Column(Modifier.fillMaxSize()) {
            BarraSuperior("Configuración", progreso.monedas, onVolver)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Tarjeta("Juego") {
                    Interruptor("Avance automático al siguiente nivel", progreso.ajustes.avanceAutomatico) { v ->
                        onCambiarAjustes { it.copy(avanceAutomatico = v) }
                    }
                    Interruptor("Vibración", progreso.ajustes.vibracion) { v ->
                        onCambiarAjustes { it.copy(vibracion = v) }
                    }
                }

                val est = progreso.estadisticas
                Tarjeta("Estadísticas") {
                    Dato("Niveles completados", "${progreso.completados.size}")
                    Dato("Niveles sin usar pistas", "${est.nivelesSinPistas}")
                    Dato("Mejor racha sin pistas", "${est.mejorRacha}")
                    Dato("Intentos fallidos", "${est.intentosFallidos}")
                    Dato("Pistas usadas", "${est.pistasUsadas}")
                    Dato("Monedas ganadas", "${est.monedasGanadas}")
                    Dato("Monedas gastadas", "${est.monedasGastadas}")
                }

                if (catalogo != null) {
                    val imagenes = catalogo.archivosImagen
                    val pendientes = imagenes.filterNot { cargador.existe(it.archivo) }
                    Tarjeta("Imágenes de niveles") {
                        Dato("Niveles cargados", "${catalogo.total}")
                        Dato("Imágenes listas", "${imagenes.size - pendientes.size} de ${imagenes.size}")
                        if (pendientes.isNotEmpty()) {
                            TextButton(onClick = { verPendientes = !verPendientes }) {
                                Text(if (verPendientes) "Ocultar pendientes" else "Ver imágenes pendientes (${pendientes.size})")
                            }
                            if (verPendientes) {
                                pendientes.forEach {
                                    Text(
                                        "• ${it.archivo}: ${it.descripcion}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Colores.Texto,
                                    )
                                }
                            }
                        }
                    }
                    if (catalogo.errores.isNotEmpty()) {
                        Tarjeta("Errores en los archivos de niveles") {
                            catalogo.errores.forEach {
                                Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Colores.RosaSombra)
                            }
                        }
                    }
                }

                BotonGrande(
                    texto = "REINICIAR PROGRESO",
                    onClick = { confirmarReinicio = true },
                    modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                    color = Colores.Rosa,
                    sombra = Colores.RosaSombra,
                    colorTexto = Colores.Blanco,
                    alto = 52.dp,
                    tamanoTexto = 17,
                )
                Text("Fotopalabra v$version · funciona sin conexión", color = Colores.Blanco.copy(alpha = 0.8f))
            }
        }
    }

    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("¿Reiniciar progreso?") },
            text = { Text("Se borrarán los niveles completados, las monedas y las estadísticas. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirmarReinicio = false; onReiniciar() }) {
                    Text("Reiniciar", color = Colores.RosaSombra, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmarReinicio = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Colores.Blanco)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Violeta)
        contenido()
    }
}

@Composable
private fun Interruptor(texto: String, valor: Boolean, onCambio: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, modifier = Modifier.weight(1f), color = Colores.Texto)
        Switch(
            checked = valor,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(checkedTrackColor = Colores.Verde),
        )
    }
}

@Composable
private fun Dato(nombre: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(nombre, modifier = Modifier.weight(1f), color = Colores.Texto)
        Text(valor, fontWeight = FontWeight.Bold, color = Colores.Texto)
    }
}
