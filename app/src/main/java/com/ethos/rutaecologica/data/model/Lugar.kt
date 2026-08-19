package com.ethos.rutaecologica.data.model

/**
 * Punto ecológico / estación del campus.
 * Se mapea 1:1 con el nodo de Firebase Realtime Database:
 * RutaEcologica/Lugares/{id}
 */
data class Lugar(
    val id: String = "",
    val nombre: String = "",
    val nombreCientifico: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estrellas: Int = 0,
    val audio: String = "",
    val imagen: String = "",
    val video: String = "",       // nuevo: URL del video que reemplaza la foto en la vista de info
    val modelo3D: String = "",    // URL del archivo .glb
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val datoCurioso: String = "",
    val estadoConservacion: String = "",
    val habitat: String = "",
    val nivel: String = "",
    val icono: String = "" // URL de la imagen sin fondo para el pasaporte
)