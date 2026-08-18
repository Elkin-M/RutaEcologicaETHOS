package com.ethos.rutaecologica.domain

import com.ethos.rutaecologica.data.model.Nivel

/**
 * Lógica de niveles y progreso.
 *
 *  >= 150 -> Leyenda Verde  | 5 insignias
 *  >= 100 -> Maestro ETHOS | 4 insignias
 *  >= 50  -> Protector     | 3 insignias
 *  >= 20  -> Guardián      | 2 insignias
 *  si no  -> Explorador    | 1 insignia
 */
object GamificationLogic {

    fun calcularNivel(estrellas: Int, totalEstrellas: Int): Nivel {
        val niveles = Nivel.todos()
        // Si no hay estrellas en la base de datos (totalEstrellas = 0), todos son Exploradores.
        if (totalEstrellas <= 0) return Nivel.EXPLORADOR
        
        return niveles.last { estrellas >= it.minEstrellas(totalEstrellas) }
    }

    /** Progreso (0f..1f) dentro del nivel actual, útil para una barra visual. */
    fun progresoHaciaSiguienteNivel(estrellas: Int, totalEstrellas: Int): Float {
        if (totalEstrellas <= 0) return 0f
        val niveles = Nivel.todos()
        val actual = calcularNivel(estrellas, totalEstrellas)
        val index = niveles.indexOf(actual)
        val siguiente = niveles.getOrNull(index + 1) ?: return 1f
        
        val minActual = actual.minEstrellas(totalEstrellas)
        val minSiguiente = siguiente.minEstrellas(totalEstrellas)
        
        val rango = (minSiguiente - minActual).toFloat()
        if (rango <= 0) return 1f
        
        val avance = (estrellas - minActual).toFloat()
        return (avance / rango).coerceIn(0f, 1f)
    }

    fun siguienteNivel(estrellas: Int, totalEstrellas: Int): Nivel? {
        if (totalEstrellas <= 0) return Nivel.GUARDIAN // El que sigue a Explorador
        val niveles = Nivel.todos()
        val actual = calcularNivel(estrellas, totalEstrellas)
        return niveles.getOrNull(niveles.indexOf(actual) + 1)
    }
}
