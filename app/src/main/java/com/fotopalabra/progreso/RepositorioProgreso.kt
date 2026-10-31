package com.fotopalabra.progreso

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/** Donde se guarda físicamente el progreso (SharedPreferences en Android, memoria en tests). */
interface Almacen {
    fun leer(): String?
    fun escribir(datos: String)
}

/** Mantiene el progreso en memoria y lo guarda automáticamente en cada cambio. */
class RepositorioProgreso(private val almacen: Almacen) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _progreso = MutableStateFlow(cargar())
    val progreso: StateFlow<Progreso> = _progreso.asStateFlow()

    val actual: Progreso get() = _progreso.value

    fun actualizar(cambio: (Progreso) -> Progreso) {
        val nuevo = cambio(_progreso.value)
        if (nuevo != _progreso.value) guardar(nuevo)
    }

    /** Borra todo el progreso, pero conserva los ajustes. */
    fun reiniciar() = guardar(Progreso(ajustes = actual.ajustes))

    private fun guardar(nuevo: Progreso) {
        _progreso.value = nuevo
        almacen.escribir(json.encodeToString(Progreso.serializer(), nuevo))
    }

    private fun cargar(): Progreso {
        val datos = almacen.leer() ?: return Progreso()
        return try {
            json.decodeFromString(Progreso.serializer(), datos)
        } catch (e: Exception) {
            Progreso()
        }
    }
}
