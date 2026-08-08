package com.ethos.rutaecologica.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ethos.rutaecologica.ui.theme.*

/** Encabezado degradado verde -> teal, usado en Inicio, Escanear, Resultado, Pasaporte. */
@Composable
fun EcoGradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EthosGreenDark, EthosGreen, EthosGreenMid)
                )
            )
            .statusBarsPadding() // Evita que el contenido quede bajo la barra de estado
            .padding(horizontal = 24.dp, vertical = 28.dp),
        content = content
    )
}

/** Chip de estrellas doradas animadas, ej. "5 Estrellas". */
@Composable
fun EstrellasChip(cantidad: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(EthosGold.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = EthosGold)
        Text(
            "$cantidad Estrellas",
            color = EthosGoldDark,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/** Insignia circular de nivel, con color propio por rango. */
@Composable
fun InsigniaNivel(nivel: String, size: Int = 72, modifier: Modifier = Modifier) {
    val color = when (nivel) {
        "Guardián" -> NivelGuardian
        "Protector" -> NivelProtector
        "Maestro ETHOS" -> NivelMaestro
        else -> NivelExplorador
    }
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color, color.copy(alpha = 0.6f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.WorkspacePremium,
            contentDescription = nivel,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

/** Barra de progreso animada hacia el siguiente nivel. */
@Composable
fun ProgresoNivelBar(progreso: Float, modifier: Modifier = Modifier) {
    val animado by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(700),
        label = "progresoNivel"
    )
    LinearProgressIndicator(
        progress = { animado },
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50)),
        color = EthosGold,
        trackColor = Color.White.copy(alpha = 0.25f)
    )
}

/** Tarjeta de acción grande para el Home (Escanear / Pasaporte / Progreso). */
@Composable
fun EcoActionCard(
    titulo: String,
    subtitulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorFondo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(subtitulo, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
        }
    }
}

/** Reproductor de video para el banner de bienvenida. */
@OptIn(UnstableApi::class)
@Composable
fun VideoBienvenida(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f // Silencio para no interrumpir al usuario
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                exoPlayer.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
