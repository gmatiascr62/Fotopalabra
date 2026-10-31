package com.fotopalabra.datos

import android.content.Context
import com.fotopalabra.progreso.Almacen

/** Guarda el progreso como un único JSON dentro de SharedPreferences (offline, sin cuentas). */
class AlmacenPreferencias(context: Context) : Almacen {

    private val prefs = context.getSharedPreferences("progreso", Context.MODE_PRIVATE)

    override fun leer(): String? = prefs.getString(CLAVE, null)

    override fun escribir(datos: String) {
        prefs.edit().putString(CLAVE, datos).apply()
    }

    private companion object {
        const val CLAVE = "progreso_v1"
    }
}
