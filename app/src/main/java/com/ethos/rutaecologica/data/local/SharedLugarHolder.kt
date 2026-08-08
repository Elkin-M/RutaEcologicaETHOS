package com.ethos.rutaecologica.data.local

import com.ethos.rutaecologica.data.model.Lugar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reemplaza el paso de "parámetro inicial de pantalla" de App Inventor.
 * ScannerViewModel escribe el lugar encontrado; ResultViewModel lo lee.
 */
@Singleton
class SharedLugarHolder @Inject constructor() {
    private val _lugarActual = MutableStateFlow<Lugar?>(null)
    val lugarActual: StateFlow<Lugar?> = _lugarActual

    fun set(lugar: Lugar) {
        _lugarActual.value = lugar
    }

    fun clear() {
        _lugarActual.value = null
    }
}
