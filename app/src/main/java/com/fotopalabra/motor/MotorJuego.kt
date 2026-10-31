package com.fotopalabra.motor

/**
 * Reglas del juego: colocar letras, quitarlas y comprobar la respuesta.
 * No depende de Android, por lo que se puede probar con tests unitarios.
 */
object MotorJuego {

    fun nuevaPartida(solucion: String, semilla: Long, totalFichas: Int? = null): EstadoPartida {
        require(Texto.esValida(solucion)) { "Solución inválida: $solucion" }
        return EstadoPartida(
            solucion = solucion,
            fichas = GeneradorLetras.generar(solucion, semilla, totalFichas),
            casilleros = List(solucion.length) { Casillero() },
        )
    }

    /** Coloca la ficha en el primer casillero libre. */
    fun colocar(estado: EstadoPartida, fichaId: Int): EstadoPartida {
        val ficha = estado.fichas.firstOrNull { it.id == fichaId } ?: return estado
        if (!estado.estaDisponible(ficha)) return estado
        val libre = estado.casilleros.indexOfFirst { it.fichaId == null }
        if (libre < 0) return estado
        return estado.copy(casilleros = estado.casilleros.reemplazar(libre, Casillero(fichaId)))
    }

    /** Devuelve a las fichas la letra del casillero indicado (salvo que la haya revelado una pista). */
    fun quitarDeCasillero(estado: EstadoPartida, indice: Int): EstadoPartida {
        val casillero = estado.casilleros.getOrNull(indice) ?: return estado
        if (casillero.revelado || casillero.fichaId == null) return estado
        return estado.copy(casilleros = estado.casilleros.reemplazar(indice, Casillero()))
    }

    /** Botón borrar: quita la última letra colocada que no sea fija. */
    fun borrarUltima(estado: EstadoPartida): EstadoPartida {
        val indice = estado.casilleros.indexOfLast { it.fichaId != null && !it.revelado }
        return if (indice < 0) estado else quitarDeCasillero(estado, indice)
    }

    /** Quita todas las letras que no fueron reveladas por pistas. */
    fun limpiar(estado: EstadoPartida): EstadoPartida =
        estado.copy(casilleros = estado.casilleros.map { if (it.revelado) it else Casillero() })

    fun evaluar(estado: EstadoPartida): Resultado = when {
        !estado.completo -> Resultado.INCOMPLETO
        estado.palabraActual == estado.solucion -> Resultado.CORRECTO
        else -> Resultado.INCORRECTO
    }
}

internal fun <T> List<T>.reemplazar(indice: Int, valor: T): List<T> =
    toMutableList().also { it[indice] = valor }

fun EstadoPartida.reemplazarCasillero(indice: Int, casillero: Casillero): EstadoPartida =
    copy(casilleros = casilleros.reemplazar(indice, casillero))
