package com.ethos.rutaecologica.data.model

/**
 * Progreso local del usuario (reemplazo de TinyDB -> DataStore).
 */
data class UserProgress(
    val usuario: String = "Estudiante",
    val nivel: String = Nivel.EXPLORADOR.etiqueta,
    val estrellas: Int = 0,
    val insignias: Int = 1,
    val lugaresVisitados: List<String> = emptyList(),
    val ultimoQR: String = ""
)

enum class Nivel(val etiqueta: String, val minEstrellas: Int, val numeroInsignias: Int) {
    EXPLORADOR("Explorador", 0, 1),
    GUARDIAN("Guardián", 20, 2),
    PROTECTOR("Protector", 50, 3),
    MAESTRO_ETHOS("Maestro ETHOS", 100, 4);

    companion object {
        fun todos() = entries.sortedBy { it.minEstrellas }
    }
}
