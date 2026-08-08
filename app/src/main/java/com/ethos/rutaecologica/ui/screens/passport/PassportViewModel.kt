package com.ethos.rutaecologica.ui.screens.passport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethos.rutaecologica.data.local.UserPreferencesRepository
import com.ethos.rutaecologica.data.model.Lugar
import com.ethos.rutaecologica.data.remote.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelloPasaporte(val lugar: Lugar, val visitado: Boolean)

@HiltViewModel
class PassportViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _lugares = MutableStateFlow<List<Lugar>>(emptyList())
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    val sellos: StateFlow<List<SelloPasaporte>> = combine(
        _lugares, prefsRepo.progresoFlow
    ) { lugares, progreso ->
        lugares.map { SelloPasaporte(it, progreso.lugaresVisitados.contains(it.id)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        cargarLugares()
    }

    private fun cargarLugares() {
        viewModelScope.launch(Dispatchers.IO) {
            _cargando.value = true
            try {
                _lugares.value = firebaseRepo.obtenerTodosLosLugares()
            } catch (e: Exception) {
                _lugares.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }
}