package com.ethos.rutaecologica.ui.screens.passport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethos.rutaecologica.ui.common.EcoGradientHeader
import com.ethos.rutaecologica.ui.theme.EthosGold
import com.ethos.rutaecologica.ui.theme.EthosGreen
import com.ethos.rutaecologica.ui.theme.EthosGreenDark
import com.ethos.rutaecologica.ui.theme.EthosSand

@Composable
fun PassportScreen(
    onVolver: () -> Unit,
    viewModel: PassportViewModel = hiltViewModel()
) {
    val sellos by viewModel.sellos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    Column(Modifier.fillMaxSize().background(EthosSand)) {
        EcoGradientHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("Pasaporte Ambiental", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${sellos.count { it.visitado }} / ${sellos.size} puntos sellados",
                        color = Color.White.copy(alpha = 0.85f)
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
                items(sellos) { sello -> SelloCard(sello) }
            }
        }
    }
}

@Composable
private fun SelloCard(sello: SelloPasaporte) {
    val colorFondo = if (sello.visitado) EthosGreen else Color.LightGray.copy(alpha = 0.35f)
    val colorIcono = if (sello.visitado) Color.White else Color.Gray

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(colorFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (sello.visitado) Icons.Filled.EmojiNature else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = colorIcono
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                sello.lugar.nombre,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = EthosGreenDark,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (sello.visitado) "+${sello.lugar.estrellas} ⭐ obtenidas" else "Por descubrir",
                color = if (sello.visitado) EthosGold.let { Color(0xFFB8860B) } else Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
