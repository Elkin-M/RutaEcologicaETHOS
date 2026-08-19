package com.ethos.rutaecologica.ui.screens.question

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ethos.rutaecologica.ui.common.EcoHeader
import com.ethos.rutaecologica.ui.theme.*

private data class Pregunta(val texto: String, val opciones: List<String>, val correctaIndex: Int)

private val preguntasDemo = listOf(
    Pregunta(
        "¿Qué función cumplen los manglares en la costa?",
        listOf("Protegen la costa y filtran el agua", "Solo dan sombra", "No cumplen ninguna función"),
        0
    ),
    Pregunta(
        "¿Cuál es la mejor práctica para reciclar correctamente?",
        listOf("Mezclar todos los residuos", "Clasificar los residuos por tipo", "Quemar la basura"),
        1
    )
)

@Composable
fun QuestionScreen(onVolver: () -> Unit) {
    var indice by remember { mutableStateOf(0) }
    var seleccion by remember { mutableStateOf(-1) }
    var puntaje by remember { mutableStateOf(0) }

    val pregunta = preguntasDemo.getOrNull(indice)

    Column(Modifier.fillMaxSize().background(EthosBackground)) {
        EcoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = EthosTextLight)
                }
                Text("Trivia Ambiental", color = EthosTextLight, style = MaterialTheme.typography.headlineMedium)
            }
        }

        if (pregunta == null) {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("¡Completaste la trivia! 🎉", style = MaterialTheme.typography.headlineMedium, color = EthosTextDark)
                Spacer(Modifier.height(8.dp))
                Text("Puntaje: $puntaje / ${preguntasDemo.size}", color = EthosTextDark)
            }
        } else {
            Column(Modifier.padding(24.dp)) {
                Text("Pregunta ${indice + 1} de ${preguntasDemo.size}", color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                Text(pregunta.texto, style = MaterialTheme.typography.titleLarge, color = EthosTextDark)
                Spacer(Modifier.height(20.dp))

                pregunta.opciones.forEachIndexed { i, opcion ->
                    val esCorrecta = i == pregunta.correctaIndex
                    val colorFondo = when {
                        seleccion == -1 -> Color.White
                        i == seleccion && esCorrecta -> EthosGreen.copy(alpha = 0.25f)
                        i == seleccion && !esCorrecta -> Color(0xFFFFCDD2)
                        esCorrecta -> EthosGreen.copy(alpha = 0.15f)
                        else -> Color.White
                    }
                    Card(
                        onClick = { if (seleccion == -1) { seleccion = i; if (esCorrecta) puntaje++ } },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = colorFondo),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(opcion, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium, color = EthosTextDark)
                    }
                }

                Spacer(Modifier.height(20.dp))
                if (seleccion != -1) {
                    Button(
                        onClick = { indice++; seleccion = -1 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EthosGreen)
                    ) { Text("Siguiente", color = Color.White) }
                }
            }
        }
    }
}
