package com.fotopalabra.datos

import android.content.res.AssetManager
import android.util.Log
import com.fotopalabra.niveles.CargadorNiveles
import com.fotopalabra.niveles.CatalogoNiveles

/** Lee todos los archivos `.json` de `assets/niveles/` y arma el catálogo de niveles. */
class FuenteNivelesAssets(private val assets: AssetManager) {

    fun cargar(): CatalogoNiveles {
        val nombres = assets.list(CARPETA).orEmpty().filter { it.endsWith(".json", ignoreCase = true) }
        val archivos = nombres.map { nombre ->
            nombre to assets.open("$CARPETA/$nombre").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        val catalogo = CargadorNiveles.cargar(archivos)
        catalogo.errores.forEach { Log.w(TAG, it) }
        Log.i(TAG, "Niveles cargados: ${catalogo.total} desde ${nombres.size} archivo(s)")
        return catalogo
    }

    companion object {
        const val CARPETA = "niveles"
        private const val TAG = "Niveles"
    }
}
