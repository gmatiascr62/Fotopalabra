package com.fotopalabra.niveles

import com.fotopalabra.motor.Texto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/** Resultado de cargar los niveles: los válidos y la lista de problemas encontrados. */
data class CatalogoNiveles(
    val niveles: List<Nivel>,
    val errores: List<String>,
) {
    private val porId = niveles.associateBy { it.id }

    val total: Int get() = niveles.size

    fun porId(id: Int): Nivel? = porId[id]

    fun indiceDe(id: Int): Int = niveles.indexOfFirst { it.id == id }

    fun siguiente(nivel: Nivel): Nivel? = niveles.getOrNull(nivel.numero)

    /** Todos los archivos de imagen referenciados, sin repetir. */
    val archivosImagen: List<Imagen> get() = niveles.flatMap { it.imagenes }.distinctBy { it.archivo }

    companion object {
        val VACIO = CatalogoNiveles(emptyList(), emptyList())
    }
}

/**
 * Convierte los archivos JSON de niveles en un [CatalogoNiveles].
 * Los niveles con errores se descartan (y se informan) en lugar de cerrar la app.
 */
object CargadorNiveles {

    const val LARGO_MAXIMO = 16
    private val ARCHIVO_VALIDO = Regex("[A-Za-z0-9_./-]+\\.(png|jpg|jpeg|webp)", RegexOption.IGNORE_CASE)

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
    }

    /** @param archivos pares (nombre del archivo, contenido), en cualquier orden. */
    fun cargar(archivos: List<Pair<String, String>>): CatalogoNiveles {
        val errores = mutableListOf<String>()
        val crudos = mutableListOf<Pair<String, NivelJson>>()

        for ((nombre, contenido) in archivos.sortedBy { it.first }) {
            try {
                json.decodeFromString(ArchivoNiveles.serializer(), contenido).niveles
                    .forEach { crudos += nombre to it }
            } catch (e: Exception) {
                errores += "$nombre: no se pudo leer el archivo (${e.message?.lineSequence()?.firstOrNull()})"
            }
        }

        val idsVistos = mutableSetOf<Int>()
        val validos = mutableListOf<Pair<NivelJson, Nivel>>()
        for ((nombre, n) in crudos) {
            val problema = validar(n, idsVistos)
            if (problema != null) {
                errores += "$nombre, nivel ${n.id}: $problema"
                continue
            }
            idsVistos += n.id
            validos += n to Nivel(
                id = n.id,
                numero = 0,
                respuesta = n.respuesta.trim(),
                solucion = Texto.normalizar(n.respuesta),
                dificultad = Dificultad.desdeTexto(n.dificultad)!!,
                imagenes = n.imagenes.map { Imagen(it.archivo.trim(), it.descripcion.trim()) },
                fichas = n.fichas,
            )
        }

        val niveles = validos.map { it.second }
            .sortedBy { it.id }
            .mapIndexed { i, nivel -> nivel.copy(numero = i + 1) }
        return CatalogoNiveles(niveles, errores)
    }

    private fun validar(n: NivelJson, idsVistos: Set<Int>): String? {
        val solucion = Texto.normalizar(n.respuesta)
        return when {
            n.id <= 0 -> "el id debe ser un número mayor que 0"
            n.id in idsVistos -> "el id ${n.id} está repetido"
            !Texto.esValida(solucion) -> "la respuesta \"${n.respuesta}\" solo puede tener letras"
            solucion.length > LARGO_MAXIMO -> "la respuesta tiene más de $LARGO_MAXIMO letras"
            Dificultad.desdeTexto(n.dificultad) == null ->
                "dificultad \"${n.dificultad}\" desconocida (usar facil, intermedio o dificil)"
            n.imagenes.isEmpty() -> "falta al menos una imagen"
            n.imagenes.any { !ARCHIVO_VALIDO.matches(it.archivo.trim()) || ".." in it.archivo } ->
                "nombre de imagen inválido (usar .png, .jpg o .webp, sin espacios)"
            n.fichas != null && n.fichas !in solucion.length..24 ->
                "\"fichas\" debe estar entre ${solucion.length} y 24"
            else -> null
        }
    }
}
