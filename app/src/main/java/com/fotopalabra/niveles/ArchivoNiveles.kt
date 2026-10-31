package com.fotopalabra.niveles

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

/** Formato de los archivos JSON de `assets/niveles`. Ver `NIVELES.md`. */
@Serializable
data class ArchivoNiveles(
    val version: Int = 1,
    val niveles: List<NivelJson> = emptyList(),
)

@Serializable
data class NivelJson(
    val id: Int,
    val respuesta: String,
    val dificultad: String = "facil",
    val imagenes: List<@Serializable(with = ImagenJsonSerializer::class) ImagenJson> = emptyList(),
    val pista: String = "",
    /** Cantidad total de letras disponibles (opcional). */
    val fichas: Int? = null,
)

@Serializable
data class ImagenJson(
    val archivo: String,
    /** Qué debe mostrar la ilustración. Se usa en el marcador temporal. */
    val descripcion: String = "",
)

/** Permite escribir una imagen como texto simple: `"nivel_001.png"`. */
object ImagenJsonSerializer : JsonTransformingSerializer<ImagenJson>(ImagenJson.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonPrimitive && element.isString) {
            JsonObject(mapOf("archivo" to element))
        } else {
            element
        }
}

