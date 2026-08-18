package com.ethos.rutaecologica.ui.screens.passport

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colorFondo),
                contentAlignment = Alignment.Center
            ) {
                if (sello.visitado && sello.lugar.imagen.isNotEmpty()) {
                    AsyncImage(
                        model = sello.lugar.imagen,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentScale = ContentScale.Fit,
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(EthosGreenPale),
                    contentAlignment = Alignment.Center
                ) {
                    if (lugar.imagen.isNotEmpty()) {
                        AsyncImage(
                            model = lugar.imagen,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            Icons.Filled.EmojiNature,
                            contentDescription = null,
                            tint = EthosGreen,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = EthosTextDark,
                    textAlign = TextAlign.Center
                )

                if (lugar.nombreCientifico.isNotEmpty()) {
                    Text(
                        text = lugar.nombreCientifico,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EthosGreen,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = lugar.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EthosTextDark.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EthosAccentTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¡Genial!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
