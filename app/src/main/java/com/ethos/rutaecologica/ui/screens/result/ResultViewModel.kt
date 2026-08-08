package com.ethos.rutaecologica.ui.screens.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.ethos.rutaecologica.data.local.SharedLugarHolder
import com.ethos.rutaecologica.data.model.Lugar
import com.ethos.rutaecologica.data.remote.Model3DDownloader
import com.ethos.rutaecologica.data.remote.ResultadoModelo3D
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    application: Application,
    sharedLugar: SharedLugarHolder,
    private val model3DDownloader: Model3DDownloader
) : AndroidViewModel(application) {

    val lugar: StateFlow<Lugar?> = sharedLugar.lugarActual

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Archivo .glb ya descargado y validado localmente, listo para mostrar
    // en el WebView. null mientras no hay modelo, está descargando, o falló.
    private val _modelo3DLocal = MutableStateFlow<File?>(null)
    val modelo3DLocal: StateFlow<File?> = _modelo3DLocal.asStateFlow()

    private val _modelo3DCargando = MutableStateFlow(false)
    val modelo3DCargando: StateFlow<Boolean> = _modelo3DCargando.asStateFlow()

    // Mensaje de error específico (red, Drive, archivo inválido, etc.) que
    // viene directo de Model3DDownloader, listo para mostrar en pantalla.
    private val _modelo3DError = MutableStateFlow<String?>(null)
    val modelo3DError: StateFlow<String?> = _modelo3DError.asStateFlow()

    // Progreso de descarga del modelo 3D: 0f..1f, o -1f si el tamaño total
    // del archivo es desconocido (Drive no mandó Content-Length), lo que
    // la UI puede usar para mostrar una barra indeterminada.
    private val _modelo3DProgreso = MutableStateFlow(0f)
    val modelo3DProgreso: StateFlow<Float> = _modelo3DProgreso.asStateFlow()

    private var player: ExoPlayer? = null
    private var ultimoLugarIdDescargado: String? = null

    init {
        // Cada vez que cambia el lugar mostrado, si tiene modelo3D y no lo
        // hemos descargado ya para ese mismo lugar, dispara la descarga.
        viewModelScope.launch {
            lugar.collect { l ->
                if (l == null) return@collect
                if (l.modelo3D.isBlank()) {
                    _modelo3DLocal.value = null
                    _modelo3DError.value = null
                    _modelo3DProgreso.value = 0f
                    ultimoLugarIdDescargado = null
                    return@collect
                }
                if (ultimoLugarIdDescargado == l.id) return@collect

                ultimoLugarIdDescargado = l.id
                descargarModelo3D(l.modelo3D)
            }
        }
    }

    private fun descargarModelo3D(urlDrive: String) {
        _modelo3DCargando.value = true
        _modelo3DError.value = null
        _modelo3DLocal.value = null
        _modelo3DProgreso.value = 0f

        viewModelScope.launch {
            // El callback llega desde Dispatchers.IO; MutableStateFlow.value
            // es thread-safe así que no hace falta cambiar de contexto.
            when (val resultado = model3DDownloader.obtenerModeloLocal(urlDrive) { progreso ->
                _modelo3DProgreso.value = progreso
            }) {
                is ResultadoModelo3D.Exito -> {
                    _modelo3DCargando.value = false
                    _modelo3DLocal.value = resultado.archivo
                }
                is ResultadoModelo3D.Error -> {
                    _modelo3DCargando.value = false
                    _modelo3DError.value = resultado.mensaje
                }
            }
        }
    }

    /** Botón "Escuchar Audio" -> Media3/ExoPlayer sobre la URL `audio` de Firebase. */
    fun playPauseAudio(urlAudio: String) {
        if (urlAudio.isBlank()) return

        if (player == null) {
            player = ExoPlayer.Builder(getApplication()).build().apply {
                setMediaItem(MediaItem.fromUri(urlAudio))
                prepare()
            }
        }

        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
            _isPlaying.value = false
        } else {
            p.play()
            _isPlaying.value = true
        }
    }

    override fun onCleared() {
        player?.release()
        player = null
        super.onCleared()
    }
}