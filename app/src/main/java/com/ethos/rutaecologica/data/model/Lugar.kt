package com.ethos.rutaecologica.data.model

/**
 * Punto ecológico / estación del campus.
 * Se mapea 1:1 con el nodo de Firebase Realtime Database:
 * RutaEcologica/Lugares/{id}
 *
 * Los campos `nombre`, `descripcion`, `estrellas`, `audio` e `imagen`
 * vienen directamente del ruta-ecologica.json original.
 * `categoria`, `latitud` y `longitud` son nuevos, pensados para
 * mostrar el punto en un mapa o agrupar en el Pasaporte.
 */
data class Lugar(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estrellas: Int = 0,
    val audio: String = "",
    val imagen: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
) {
    companion object {
        const val NO_EXISTE = "NO_EXISTE"
    }
}
