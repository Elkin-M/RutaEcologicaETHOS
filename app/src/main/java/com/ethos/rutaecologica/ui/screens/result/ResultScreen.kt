package com.ethos.rutaecologica.ui.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ethos.rutaecologica.ui.common.EstrellasChip
import com.ethos.rutaecologica.ui.theme.EthosGold
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

    Column(Modifier.fillMaxSize()) {
        Box {
            AsyncImage(
                model = datos.imagen,
                contentDescription = datos.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Crop
            )
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
                .fillMaxSize()
                .background(EthosSand)
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

            Spacer(Modifier.weight(1f))

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
