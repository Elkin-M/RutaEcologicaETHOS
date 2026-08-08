package com.ethos.rutaecologica.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethos.rutaecologica.data.local.UserPreferencesRepository
import com.ethos.rutaecologica.data.model.UserProgress
import com.ethos.rutaecologica.domain.GamificationLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    // Inicio.Inicializar: lee usuario, estrellas, nivel, insignias, lugaresVisitados
    val progreso: StateFlow<UserProgress> = prefsRepo.progresoFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProgress()
    )

    fun progresoHaciaSiguienteNivel(estrellas: Int) =
        GamificationLogic.progresoHaciaSiguienteNivel(estrellas)

    fun siguienteNivelLabel(estrellas: Int): String =
        GamificationLogic.siguienteNivel(estrellas)?.etiqueta ?: "¡Nivel máximo!"

    /**
     * Evento OtraPantallaCerrada: al volver de Resultado, si el lugar es nuevo
     * se suman estrellas y se recalcula nivel/insignias (todo dentro del repo).
     */
    fun registrarVisita(lugarId: String, estrellasGanadas: Int, onNuevo: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val esNuevo = prefsRepo.registrarVisita(lugarId, estrellasGanadas)
            onNuevo(esNuevo)
        }
    }

    fun actualizarNombreUsuario(nombre: String) {
        viewModelScope.launch { prefsRepo.guardarUsuario(nombre) }
    }
}
