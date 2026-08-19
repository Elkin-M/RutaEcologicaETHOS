package com.ethos.rutaecologica.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethos.rutaecologica.R
import com.ethos.rutaecologica.ui.common.EcoActionCard
import com.ethos.rutaecologica.ui.common.EstrellasChip
import com.ethos.rutaecologica.ui.common.ProgresoNivelBar
import com.ethos.rutaecologica.ui.theme.*

@Composable
fun HomeScreen(
    onIrAEscanear: () -> Unit,
    onIrAPasaporte: () -> Unit,
    onIrAProgreso: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val progreso by viewModel.progreso.collectAsState()

    HomeContent(
        usuario = progreso.usuario,
        nivel = progreso.nivel,
        estrellas = progreso.estrellas,
        lugaresVisitadosSize = progreso.lugaresVisitados.size,
        insignias = progreso.insignias,
        siguienteNivelLabel = viewModel.siguienteNivelLabel(progreso.estrellas),
        progresoHaciaSiguienteNivel = viewModel.progresoHaciaSiguienteNivel(progreso.estrellas),
        onIrAEscanear = onIrAEscanear,
        onIrAPasaporte = onIrAPasaporte,
        onIrAProgreso = onIrAProgreso
    )
}

@Composable
fun HomeContent(
    usuario: String,
    nivel: String,
    estrellas: Int,
    lugaresVisitadosSize: Int,
    insignias: Int,
    siguienteNivelLabel: String,
    progresoHaciaSiguienteNivel: Float,
    onIrAEscanear: () -> Unit,
    onIrAPasaporte: () -> Unit,
    onIrAProgreso: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(EthosBackground)) {
        val isSmallScreen = maxHeight < 700.dp
        // Banner ajustado (-20% de tamaño total para balance óptimo)
        val headerHeight = if (isSmallScreen) 304.dp else 380.dp
        // Margen del 5% exacto de la pantalla para los botones
        val horizontalMargin = maxWidth * 0.05f
        val cardSpacing = if (isSmallScreen) 12.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Header con Imagen de Banner ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Degradado reforzado para legibilidad extrema del título
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = if (isSmallScreen) 28.dp else 40.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        "RUTA ECOLÓGICA ETHOS",
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSmallScreen) 14.sp else 16.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "¡Hola, $usuario! 🌿",
                        color = Color.White,
                        style = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isSmallScreen) 38.sp else 46.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(0f, 6f),
                                blurRadius = 15f
                            )
                        )
                    )
                    Spacer(Modifier.height(if (isSmallScreen) 24.dp else 36.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Nivel: $nivel",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isSmallScreen) 18.sp else 22.sp
                            )
                            Text(
                                "Siguiente rango: $siguienteNivelLabel",
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                        EstrellasChip(cantidad = estrellas)
                    }
                    Spacer(Modifier.height(20.dp))
                    ProgresoNivelBar(
                        progreso = progresoHaciaSiguienteNivel,
                        color = EthosPrimaryYellow
                    )
                }
            }

            // ---- Tarjetas de acción ----
            Column(
                modifier = Modifier
                    .padding(horizontal = horizontalMargin, vertical = 32.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                Text(
                    "¿Qué quieres hacer hoy?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (isSmallScreen) 20.sp else 24.sp,
                    color = EthosTextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
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
                    subtitulo = "$lugaresVisitadosSize lugares visitados · $insignias insignias",
                    icono = Icons.Filled.Badge,
                    colorFondo = EthosGreen,
                    colorTexto = Color.White,
                    onClick = onIrAPasaporte
                )
                EcoActionCard(
                    titulo = "Mi Progreso",
                    subtitulo = "Estadísticas y evolución de tu ruta",
                    icono = Icons.Filled.BarChart,
                    colorFondo = EthosGreenMid,
                    colorTexto = Color.White,
                    onClick = onIrAProgreso
                )
                
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    RutaEcologicaTheme {
        HomeContent(
            usuario = "Visitante",
            nivel = "Explorador",
            estrellas = 450,
            lugaresVisitadosSize = 5,
            insignias = 3,
            siguienteNivelLabel = "Guardián",
            progresoHaciaSiguienteNivel = 0.45f,
            onIrAEscanear = {},
            onIrAPasaporte = {},
            onIrAProgreso = {}
        )
    }
}
