package com.ethos.rutaecologica.data.model

/**
 * Progreso local del usuario (reemplazo de TinyDB -> DataStore).
 */
data class UserProgress(
    val usuario: String = "",
    val nivel: String = Nivel.EXPLORADOR.etiqueta,
    val estrellas: Int = 0,
    val insignias: Int = 1,
    val lugaresVisitados: List<String> = emptyList(),
    val ultimoQR: String = ""
)

enum class Nivel(val etiqueta: String, val numeroInsignias: Int, val porcentaje: Float) {
    EXPLORADOR("Explorador", 1, 0.0f),
    GUARDIAN("Guardián", 2, 0.2f),
    PROTECTOR("Protector", 3, 0.5f),
    MAESTRO_ETHOS("Maestro ETHOS", 4, 0.8f),
    LEYENDA_VERDE("Leyenda Verde", 5, 1.0f);

    fun minEstrellas(totalEstrellas: Int): Int {
        return (totalEstrellas * porcentaje).toInt()
    }

    companion object {
        fun todos() = entries.toList()
    }
}
