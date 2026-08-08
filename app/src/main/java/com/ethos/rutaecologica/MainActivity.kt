package com.ethos.rutaecologica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ethos.rutaecologica.data.remote.FirebaseRepository
import com.ethos.rutaecologica.ui.navigation.NavGraph
import com.ethos.rutaecologica.ui.theme.RutaEcologicaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseRepo: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔌 Diagnóstico: escucha en tiempo real si el dispositivo logra
        // establecer conexión con Firebase. Revisa el log filtrando por
        // "TRACER_FIREBASE" y busca la línea "¿Conectado a Firebase?:".
        firebaseRepo.verificarConexion()

        setContent {
            RutaEcologicaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}