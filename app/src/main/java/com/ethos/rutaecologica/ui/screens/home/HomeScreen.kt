package com.ethos.rutaecologica.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethos.rutaecologica.ui.common.EcoActionCard
import com.ethos.rutaecologica.ui.common.EcoGradientHeader
import com.ethos.rutaecologica.ui.common.EstrellasChip
import com.ethos.rutaecologica.ui.common.ProgresoNivelBar
import com.ethos.rutaecologica.ui.theme.*
import androidx.compose.runtime.collectAsState

@Composable
fun HomeScreen(
    onIrAEscanear: () -> Unit,
    onIrAPasaporte: () -> Unit,
    onIrAProgreso: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val progreso by viewModel.progreso.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EthosSand)
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Header degradado: saludo + nivel + estrellas ----
        EcoGradientHeader {
            Text(
                "Ruta Ecológica ETHOS",
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Hola, ${progreso.usuario} 🌿",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Nivel: ${progreso.nivel}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstrellasChip(cantidad = progreso.estrellas)
            }
            Spacer(Modifier.height(10.dp))
            ProgresoNivelBar(progreso = viewModel.progresoHaciaSiguienteNivel(progreso.estrellas))
            Spacer(Modifier.height(4.dp))
            Text(
                "Siguiente rango: ${viewModel.siguienteNivelLabel(progreso.estrellas)}",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // ---- Tarjetas de acción ----
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "¿Qué quieres hacer hoy?",
                style = MaterialTheme.typography.titleLarge,
                color = EthosGreenDark
            )

            EcoActionCard(
                titulo = "Escanear Punto Ecológico",
                subtitulo = "Encuentra un tótem con QR en el campus",
                icono = Icons.Filled.QrCodeScanner,
                colorFondo = EthosGreen,
                onClick = onIrAEscanear
            )
            EcoActionCard(
                titulo = "Pasaporte Ambiental",
                subtitulo = "${progreso.lugaresVisitados.size} lugares visitados · ${progreso.insignias} insignias",
                icono = Icons.Filled.Badge,
                colorFondo = EthosTeal,
                onClick = onIrAPasaporte
            )
            EcoActionCard(
                titulo = "Mi Progreso",
                subtitulo = "Estadísticas y evolución de tu ruta",
                icono = Icons.Filled.BarChart,
                colorFondo = EthosBrown,
                onClick = onIrAProgreso
            )
        }
    }
}
