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
import com.ethos.rutaecologica.ui.common.EcoHeader
import com.ethos.rutaecologica.ui.common.EstrellasChip
import com.ethos.rutaecologica.ui.common.ProgresoNivelBar
import com.ethos.rutaecologica.ui.theme.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp

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
            .background(EthosBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Header sólido: saludo + nivel + estrellas ----
        EcoHeader {
            Text(
                "Ruta Ecológica ETHOS",
                color = EthosTextLight.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "¡Hola, ${progreso.usuario}! 🌿",
                color = EthosTextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Nivel: ${progreso.nivel}",
                        color = EthosTextLight.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        "Siguiente rango: ${viewModel.siguienteNivelLabel(progreso.estrellas)}",
                        color = EthosTextLight.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
                EstrellasChip(cantidad = progreso.estrellas)
            }
            Spacer(Modifier.height(6.dp))
            ProgresoNivelBar(
                progreso = viewModel.progresoHaciaSiguienteNivel(progreso.estrellas),
                color = EthosPrimaryYellow
            )
        }

        // ---- Tarjetas de acción ----
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "¿Qué quieres hacer hoy?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = EthosTextDark
            )

            EcoActionCard(
                titulo = "Escanear Punto Ecológico",
                subtitulo = "Encuentra un tótem con QR en el campus",
                icono = Icons.Filled.QrCodeScanner,
                colorFondo = EthosPrimaryYellow,
                colorTexto = EthosTextDark,
                onClick = onIrAEscanear
            )
            EcoActionCard(
                titulo = "Pasaporte Ambiental",
                subtitulo = "${progreso.lugaresVisitados.size} lugares visitados · ${progreso.insignias} insignias",
                icono = Icons.Filled.Badge,
                colorFondo = EthosSecondarySlate,
                colorTexto = EthosTextLight,
                onClick = onIrAPasaporte
            )
            EcoActionCard(
                titulo = "Mi Progreso",
                subtitulo = "Estadísticas y evolución de tu ruta",
                icono = Icons.Filled.BarChart,
                colorFondo = EthosAccentTeal,
                colorTexto = EthosTextLight,
                onClick = onIrAProgreso
            )
        }
    }
}
