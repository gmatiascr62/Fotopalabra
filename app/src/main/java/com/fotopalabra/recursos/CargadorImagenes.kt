package com.fotopalabra.recursos

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fotopalabra.datos.FuenteNivelesAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Carga las ilustraciones de `assets/niveles/` reduciéndolas al tamaño de pantalla
 * para ahorrar memoria, y mantiene en caché solo las más recientes.
 */
class CargadorImagenes(private val assets: AssetManager) {

    private val existentes: Set<String> by lazy {
        assets.list(FuenteNivelesAssets.CARPETA).orEmpty().toSet()
    }

    // Caché limitada a 1/8 de la memoria disponible para la app.
    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8 / 1024).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun existe(archivo: String): Boolean = archivo in existentes

    suspend fun cargar(archivo: String, ladoMaximo: Int = 1024): ImageBitmap? {
        if (!existe(archivo)) return null
        val clave = "$archivo@$ladoMaximo"
        cache.get(clave)?.let { return it.asImageBitmap() }
        return withContext(Dispatchers.IO) {
            try {
                val ruta = "${FuenteNivelesAssets.CARPETA}/$archivo"
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                assets.open(ruta).use { BitmapFactory.decodeStream(it, null, limites) }
                var muestreo = 1
                while (maxOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= ladoMaximo) muestreo *= 2
                val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
                val bitmap = assets.open(ruta).use { BitmapFactory.decodeStream(it, null, opciones) }
                bitmap?.also { cache.put(clave, it) }?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
}
