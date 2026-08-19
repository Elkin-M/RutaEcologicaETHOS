package com.ethos.rutaecologica.ui.screens.passport

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ethos.rutaecologica.data.model.Lugar
import com.ethos.rutaecologica.ui.common.EcoHeader
import com.ethos.rutaecologica.ui.theme.*

@Composable
fun PassportScreen(
    onVolver: () -> Unit,
    viewModel: PassportViewModel = hiltViewModel()
) {
    val sellos by viewModel.sellos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    var lugarSeleccionado by remember { mutableStateOf<Lugar?>(null) }

    Column(Modifier.fillMaxSize().background(EthosBackground)) {
        EcoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = EthosTextLight)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("Pasaporte Ambiental", color = EthosTextLight, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${sellos.count { it.visitado }} / ${sellos.size} puntos sellados",
                        color = EthosTextLight.copy(alpha = 0.85f)
                    )
                }
            }
        }

        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sellos) { sello ->
                    SelloCard(sello) {
                        if (sello.visitado) lugarSeleccionado = sello.lugar
                    }
                }
            }
        }
    }

    lugarSeleccionado?.let { lugar ->
        DetalleSelloDialog(lugar = lugar, onDismiss = { lugarSeleccionado = null })
    }
}

@Composable
private fun SelloCard(sello: SelloPasaporte, onClick: () -> Unit) {
    val colorFondo = if (sello.visitado) Color(0xFFE0E0E0) else Color.LightGray.copy(alpha = 0.35f)
    val alphaImagen = if (sello.visitado) 1f else 0.3f

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp) // Tamaño del contenedor fijo
                    .clip(CircleShape)
                    .background(if (sello.visitado) EthosGreen.copy(alpha = 0.02f) else colorFondo)
                    .border(
                        width = 0.5.dp, // Borde mínimo para no quitar espacio
                        color = if (sello.visitado) EthosGreen.copy(alpha = 0.2f) else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Prioridad: 1. Icono sin fondo, 2. Imagen normal
                val imagenAMostrar = sello.lugar.icono.ifEmpty { sello.lugar.imagen }
                
                if (sello.visitado && imagenAMostrar.isNotEmpty()) {
                    AsyncImage(
                        model = imagenAMostrar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(), // Rellena el 100% del Box
                        contentScale = ContentScale.FillBounds, // Estira para llenar el círculo
                        alpha = alphaImagen
                    )
                } else {
                    Icon(
                        if (sello.visitado) Icons.Filled.EmojiNature else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (sello.visitado) EthosGreen else Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                sello.lugar.nombre,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = EthosTextDark,
                fontSize = 14.sp,
                maxLines = 2,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (sello.visitado) "${sello.lugar.estrellas} ⭐ obtenidas" else "Por descubrir",
                color = if (sello.visitado) EthosGoldDark else Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun DetalleSelloDialog(lugar: Lugar, onDismiss: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                // Se adapta al contenido, pero no supera el 85% de la pantalla
                .heightIn(max = screenHeight * 0.85f)
                .padding(vertical = 16.dp)
                .animateContentSize(), // Transición suave al cargar la imagen
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Contenedor scrolleable
                Column(
                    modifier = Modifier
                        // fill = false es vital: permite que el modal sea pequeño si hay poco texto
                        // pero weight(1f) permite que crezca y scrollee si hay mucho
                        .weight(1f, fill = false) 
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Estado para que el contenedor del animal sea dinámico
                    var mediaAspectRatio by remember { mutableFloatStateOf(1.2f) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(mediaAspectRatio)
                            .clip(RoundedCornerShape(24.dp))
                            .background(EthosGreen.copy(alpha = 0.03f))
                            .border(1.dp, EthosGreen.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val imagenAMostrar = lugar.icono.ifEmpty { lugar.imagen }
                        
                        if (imagenAMostrar.isNotEmpty()) {
                            AsyncImage(
                                model = imagenAMostrar,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                contentScale = ContentScale.Fit,
                                onSuccess = { state ->
                                    val size = state.painter.intrinsicSize
                                    if (size.width > 0 && size.height > 0) {
                                        mediaAspectRatio = (size.width / size.height).coerceIn(0.8f, 1.8f)
                                    }
                                }
                            )
                        } else {
                            Icon(
                                Icons.Filled.EmojiNature,
                                contentDescription = null,
                                tint = EthosGreen.copy(alpha = 0.5f),
                                modifier = Modifier.size(70.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Bloque de Títulos
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = lugar.nombre,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = EthosTextDark,
                            textAlign = TextAlign.Center
                        )
                        
                        if (lugar.nombreCientifico.isNotEmpty()) {
                            Surface(
                                color = EthosGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = lugar.nombreCientifico,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = EthosGreen
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Descripción
                    Text(
                        text = lugar.descripcion,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = EthosTextDark.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Botón siempre visible al fondo
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EthosGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        "¡GENIAL!", 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}
