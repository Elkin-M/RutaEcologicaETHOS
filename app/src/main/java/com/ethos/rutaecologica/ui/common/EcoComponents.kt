package com.ethos.rutaecologica.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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

/** Encabezado sólido azul marino, usado en Inicio, Escanear, Resultado, Pasaporte. */
@Composable
fun EcoHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EthosHeaderNavy)
            .statusBarsPadding()
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

/** Insignia circular de nivel, con color y forma propia por rango. */
@Composable
fun InsigniaNivel(nivel: String, size: Int = 72, modifier: Modifier = Modifier) {
    val color = when (nivel) {
        "Guardián" -> NivelGuardian
        "Protector" -> NivelProtector
        "Maestro ETHOS" -> NivelMaestro
        "Leyenda Verde" -> NivelLeyenda
        else -> NivelExplorador
    }

    val shape = when (nivel) {
        "Guardián" -> RoundedCornerShape(12.dp)
        "Protector" -> RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp)
        "Maestro ETHOS" -> CutCornerShape(16.dp)
        "Leyenda Verde" -> StarShape()
        else -> CircleShape
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(shape)
            .background(Brush.radialGradient(listOf(color, color.copy(alpha = 0.6f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (nivel == "Leyenda Verde") Icons.Filled.AutoAwesome else Icons.Filled.WorkspacePremium,
            contentDescription = nivel,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

/** Forma de estrella simple para la insignia de máximo nivel. */
fun StarShape(): Shape = GenericShape { size, _ ->
    val center = size.width / 2f
    val radius = size.width / 2f
    val innerRadius = radius * 0.4f
    val points = 5
    var angle = -Math.PI / 2
    val nextAngle = Math.PI / points

    moveTo(
        (center + radius * Math.cos(angle)).toFloat(),
        (center + radius * Math.sin(angle)).toFloat()
    )

    for (i in 1..points) {
        angle += nextAngle
        lineTo(
            (center + innerRadius * Math.cos(angle)).toFloat(),
            (center + innerRadius * Math.sin(angle)).toFloat()
        )
        angle += nextAngle
        lineTo(
            (center + radius * Math.cos(angle)).toFloat(),
            (center + radius * Math.sin(angle)).toFloat()
        )
    }
    close()
}

/** Barra de progreso animada hacia el siguiente nivel. */
@Composable
fun ProgresoNivelBar(
    progreso: Float,
    modifier: Modifier = Modifier,
    color: Color = EthosGold
) {
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
        color = color,
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
    colorTexto: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Diseño Flat
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
                    .background(colorTexto.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colorTexto)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    color = colorTexto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    subtitulo,
                    color = colorTexto.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
