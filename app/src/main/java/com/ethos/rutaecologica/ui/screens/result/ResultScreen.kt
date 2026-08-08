package com.ethos.rutaecologica.ui.screens.result

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.ethos.rutaecologica.ui.common.EstrellasChip
import com.ethos.rutaecologica.ui.theme.EthosGreen
import com.ethos.rutaecologica.ui.theme.EthosGreenDark
import com.ethos.rutaecologica.ui.theme.EthosSand

@Composable
fun ResultScreen(
    onContinuar: (lugarId: String, estrellas: Int) -> Unit,
    onVolver: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val lugar by viewModel.lugar.collectAsState()
    val reproduciendo by viewModel.isPlaying.collectAsState()

    val datos = lugar ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(EthosSand)
    ) {
        // Header: video si existe, si no la imagen de siempre como respaldo
        Box {
            if (datos.video.isNotBlank()) {
                VideoHeader(url = datos.video)
            } else {
                AsyncImage(
                    model = datos.imagen,
                    contentDescription = datos.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (datos.categoria.isNotBlank()) {
                AssistChip(onClick = {}, label = { Text(datos.categoria) })
                Spacer(Modifier.height(10.dp))
            }

            Text(
                datos.nombre,
                style = MaterialTheme.typography.headlineMedium,
                color = EthosGreenDark
            )
            Spacer(Modifier.height(10.dp))
            EstrellasChip(cantidad = datos.estrellas)
            Spacer(Modifier.height(18.dp))

            Text(
                datos.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = EthosGreenDark.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(20.dp))

            if (datos.audio.isNotBlank()) {
                OutlinedButton(
                    onClick = { viewModel.playPauseAudio(datos.audio) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        if (reproduciendo) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (reproduciendo) "Pausar audioguía" else "Escuchar audioguía")
                }
                Spacer(Modifier.height(12.dp))
            }

            // Visor 3D: solo aparece si el lugar tiene modelo3D cargado
            if (datos.modelo3D.isNotBlank()) {
                Text(
                    "Modelo 3D",
                    style = MaterialTheme.typography.titleMedium,
                    color = EthosGreenDark
                )
                Spacer(Modifier.height(8.dp))
                Model3DViewer(
                    glbUrl = datos.modelo3D,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onContinuar(datos.id, datos.estrellas) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EthosGreen)
            ) {
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Header de video usando Media3/ExoPlayer, mismo tamaño que tenía la
 * imagen original (260dp de alto).
 */
@Composable
private fun VideoHeader(url: String) {
    val context = LocalContext.current

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )
}

/**
 * Visor de modelos 3D en formato .glb usando el web component
 * <model-viewer> de Google dentro de un WebView. Es la forma más
 * simple de mostrar .glb en Android sin agregar una librería 3D
 * pesada como Filament o Sceneform.
 *
 * IMPORTANTE: requiere permiso de Internet en el manifest (ya lo
 * tienes, porque el resto de la app usa red) y JavaScript habilitado
 * en el WebView, que se activa aquí mismo.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Model3DViewer(glbUrl: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(0x00000000)
            }
        },
        update = { webView ->
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <script type="module" src="https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js"></script>
                  <style>
                    html, body { margin:0; padding:0; height:100%; background:transparent; }
                    model-viewer { width:100%; height:100%; --poster-color: transparent; }
                  </style>
                </head>
                <body>
                  <model-viewer
                    src="$glbUrl"
                    camera-controls
                    auto-rotate
                    shadow-intensity="1"
                    exposure="1"
                    style="width:100%;height:100%;">
                  </model-viewer>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(
                "https://appassets.androidplatform.net/",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
    )
}