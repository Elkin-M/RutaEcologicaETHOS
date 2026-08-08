package com.ethos.rutaecologica.domain

import com.ethos.rutaecologica.data.model.Nivel

/**
 * Traducción directa del procedimiento `ActualizarNivel` de App Inventor:
 *
 *  >= 100 -> Maestro ETHOS | 4 insignias
 *  >= 50  -> Protector     | 3 insignias
 *  >= 20  -> Guardián      | 2 insignias
 *  si no  -> Explorador    | 1 insignia
 */
object GamificationLogic {

    fun calcularNivel(estrellas: Int): Nivel {
        return Nivel.todos().last { estrellas >= it.minEstrellas }
    }

    /** Progreso (0f..1f) dentro del nivel actual, útil para una barra visual. */
    fun progresoHaciaSiguienteNivel(estrellas: Int): Float {
        val niveles = Nivel.todos()
        val actual = calcularNivel(estrellas)
        val index = niveles.indexOf(actual)
        val siguiente = niveles.getOrNull(index + 1) ?: return 1f
        val rango = (siguiente.minEstrellas - actual.minEstrellas).toFloat()
        val avance = (estrellas - actual.minEstrellas).toFloat()
        return (avance / rango).coerceIn(0f, 1f)
    }

    fun siguienteNivel(estrellas: Int): Nivel? {
        val niveles = Nivel.todos()
        val actual = calcularNivel(estrellas)
        return niveles.getOrNull(niveles.indexOf(actual) + 1)
    }
}
