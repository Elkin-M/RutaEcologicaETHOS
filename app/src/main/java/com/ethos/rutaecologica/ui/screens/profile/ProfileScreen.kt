package com.ethos.rutaecologica.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethos.rutaecologica.data.model.Nivel
import com.ethos.rutaecologica.ui.common.EcoHeader
import com.ethos.rutaecologica.ui.common.InsigniaNivel
import com.ethos.rutaecologica.ui.screens.home.HomeViewModel
import com.ethos.rutaecologica.ui.theme.*

@Composable
fun ProfileScreen(
    onVolver: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val progreso by viewModel.progreso.collectAsState()
    val totalEstrellas by viewModel.totalEstrellas.collectAsState()

    Column(Modifier.fillMaxSize().background(EthosBackground)) {
        EcoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = EthosTextLight)
                }
                Text("Mi Progreso", color = EthosTextLight, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InsigniaNivel(nivel = progreso.nivel, size = 64)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(progreso.nivel, color = EthosTextLight, style = MaterialTheme.typography.titleLarge)
                    Text("${progreso.estrellas} estrellas de $totalEstrellas · ${progreso.insignias} insignias", color = EthosTextLight.copy(alpha = 0.85f))
                }
            }
        }

        Column(Modifier.padding(24.dp)) {
            Text("Línea de rangos", style = MaterialTheme.typography.titleLarge, color = EthosTextDark)
            Spacer(Modifier.height(16.dp))

            Nivel.todos().forEach { nivel ->
                val minEstrellas = nivel.minEstrellas(totalEstrellas)
                val alcanzado = progreso.estrellas >= minEstrellas
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (alcanzado) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (alcanzado) EthosAccentTeal else Color.Gray
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(nivel.etiqueta, fontWeight = FontWeight.Bold, color = EthosTextDark)
                        Text("Desde $minEstrellas estrellas · ${nivel.numeroInsignias} insignias", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Lugares visitados", fontWeight = FontWeight.Bold, color = EthosTextDark)
                    Spacer(Modifier.height(8.dp))
                    if (progreso.lugaresVisitados.isEmpty()) {
                        Text("Aún no has escaneado ningún punto. ¡Empieza tu ruta!", color = Color.Gray)
                    } else {
                        progreso.lugaresVisitados.forEach {
                            Text("• $it", color = EthosTextDark)
                        }
                    }
                }
            }
        }
    }
}
