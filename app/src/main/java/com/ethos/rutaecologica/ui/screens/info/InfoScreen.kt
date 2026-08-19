package com.ethos.rutaecologica.ui.screens.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ethos.rutaecologica.ui.common.EcoHeader
import com.ethos.rutaecologica.ui.theme.*

@Composable
fun InfoScreen(onVolver: () -> Unit) {
    Column(Modifier.fillMaxSize().background(EthosBackground)) {
        EcoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = EthosTextLight)
                }
                Text("Información", color = EthosTextLight, style = MaterialTheme.typography.headlineMedium)
            }
        }
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Park, contentDescription = null, tint = EthosGreen, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                "Ruta Ecológica ETHOS es una plataforma educativa gamificada que transforma el aprendizaje ambiental " +
                    "dentro del campus en una experiencia interactiva, invitando a la comunidad educativa a recorrer " +
                    "y conocer las estaciones ambientales mediante códigos QR.",
                color = EthosTextDark,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))
            Text("Un proyecto ETHOS · SENA", color = EthosTextDark, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    RutaEcologicaTheme {
        InfoScreen(onVolver = {})
    }
}
