package com.ethos.rutaecologica.ui.screens.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.ethos.rutaecologica.data.local.SharedLugarHolder
import com.ethos.rutaecologica.data.model.Lugar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    application: Application,
    sharedLugar: SharedLugarHolder
) : AndroidViewModel(application) {

    val lugar: StateFlow<Lugar?> = sharedLugar.lugarActual

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var player: ExoPlayer? = null

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
