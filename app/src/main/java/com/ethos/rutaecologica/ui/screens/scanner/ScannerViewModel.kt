package com.ethos.rutaecologica.ui.screens.scanner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethos.rutaecologica.data.local.SharedLugarHolder
import com.ethos.rutaecologica.data.local.UserPreferencesRepository
import com.ethos.rutaecologica.data.remote.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScannerEvent {
    data object Idle : ScannerEvent
    data object Buscando : ScannerEvent
    data object Encontrado : ScannerEvent
    data object NoExiste : ScannerEvent
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val sharedLugar: SharedLugarHolder
) : ViewModel() {

    private val TAG = "TRACER_VM"

    private val _estado = MutableStateFlow<ScannerEvent>(ScannerEvent.Idle)
    val estado: StateFlow<ScannerEvent> = _estado.asStateFlow()

    private var yaProcesando = false

    fun onCodigoEscaneado(codigoRaw: String) {
        Log.d(TAG, "📲 [SCANNER] Evento escáner recibido. Raw: '$codigoRaw' | yaProcesando: $yaProcesando")

        if (codigoRaw.isBlank()) {
            Log.w(TAG, "⚠️ Se descartó el código porque está vacío / en blanco.")
            return
        }

        if (yaProcesando) {
            Log.w(TAG, "⚠️ Se ignoró el escaneo porque ya hay una búsqueda en progreso.")
            return
        }

        val codigoLimpio = codigoRaw.trim().substringAfterLast("/")
        Log.d(TAG, "🧹 Código procesado/limpio: '$codigoLimpio'")

        if (codigoLimpio.isBlank()) {
            Log.w(TAG, "⚠️ El código quedó vacío después de limpiar.")
            return
        }

        yaProcesando = true
        _estado.value = ScannerEvent.Buscando
        Log.d(TAG, "🔄 Estado cambiado a: Buscando")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "💾 Guardando último QR en preferencias...")
                prefsRepo.guardarUltimoQR(codigoLimpio)

                Log.d(TAG, "📞 Llamando a firebaseRepo.obtenerLugar('$codigoLimpio')...")
                val lugar = firebaseRepo.obtenerLugar(codigoLimpio)

                if (lugar != null) {
                    Log.d(TAG, "🎉 ¡Lugar encontrado en VM! Nombre: '${lugar.nombre}'")
                    sharedLugar.set(lugar)
                    _estado.value = ScannerEvent.Encontrado
                    Log.d(TAG, "🎯 Estado cambiado a: Encontrado")
                } else {
                    Log.w(TAG, "❌ El lugar retornó NULL (No existe o hubo Timeout)")
                    _estado.value = ScannerEvent.NoExiste
                    Log.d(TAG, "🎯 Estado cambiado a: NoExiste")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Excepción no controlada en el ViewModel", e)
                _estado.value = ScannerEvent.NoExiste
            } finally {
                yaProcesando = false
                Log.d(TAG, "🏁 Flujo terminado. yaProcesando restablecido a 'false'")
            }
        }
    }

    fun reiniciar() {
        Log.d(TAG, "🔄 Reiniciando ViewModel estado a Idle")
        yaProcesando = false
        _estado.value = ScannerEvent.Idle
    }
}