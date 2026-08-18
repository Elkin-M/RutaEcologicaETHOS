package com.ethos.rutaecologica.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethos.rutaecologica.ui.screens.home.HomeViewModel
import com.ethos.rutaecologica.ui.theme.*

@Composable
fun LoginScreen(
    onIngresar: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val progreso by viewModel.progreso.collectAsState()
    var nombre by remember { mutableStateOf("") }

    // Si ya existe un nombre guardado, entramos directamente a la ruta.
    LaunchedEffect(progreso.usuario) {
        if (progreso.usuario.isNotEmpty()) {
            onIngresar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EthosHeaderNavy)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Park,
            contentDescription = null,
            tint = EthosPrimaryYellow,
            modifier = Modifier.size(88.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Ruta Ecológica ETHOS",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tu pasaporte ecológico interactivo por el campus",
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(40.dp))

        Card(
            shape = RoundedCornerShape(20.dp), 
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("¿Cómo te llamas?", fontWeight = FontWeight.Bold, color = EthosTextDark)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Ej. Estudiante SENA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val usuario = nombre.ifBlank { "Estudiante" }
                        viewModel.actualizarNombreUsuario(usuario)
                        onIngresar()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EthosAccentTeal)
                ) {
                    Text("Comenzar mi ruta 🌱", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
