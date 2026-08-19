package com.ethos.rutaecologica.ui.screens.scanner

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethos.rutaecologica.ui.common.EcoHeader
import com.ethos.rutaecologica.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onLugarEncontrado: () -> Unit,
    onVolver: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val permisoCamara = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(estado) {
        if (estado is ScannerEvent.Encontrado) onLugarEncontrado()
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        EcoHeader {
            Text("Escanear Código QR", color = EthosTextLight, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Apunta la cámara al QR del punto ecológico",
                color = EthosTextLight.copy(alpha = 0.85f)
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (permisoCamara.status.isGranted) {
                CameraPreview(
                    activo = estado is ScannerEvent.Idle,
                    onQrDetectado = viewModel::onCodigoEscaneado
                )
                ScannerFrameOverlay()

                when (estado) {
                    is ScannerEvent.Buscando -> EstadoOverlay("Consultando base de datos…")
                    is ScannerEvent.NoExiste -> EstadoOverlay(
                        "⚠️ Este QR no pertenece a un punto registrado",
                        esError = true,
                        onReintentar = viewModel::reiniciar
                    )
                    else -> {}
                }
            } else {
                PermisoRequerido(onSolicitar = { permisoCamara.launchPermissionRequest() })
            }
        }

        Text(
            "Consejo: mantén el código a 15–20 cm y evita reflejos de luz",
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CameraPreview(activo: Boolean, onQrDetectado: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(activo) {
        if (!activo) return@LaunchedEffect
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
            ).build()

        val analysis = androidx.camera.core.ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(Executors.newSingleThreadExecutor(), QrAnalyzer(onQrDetectado)) }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
        )
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun ScannerFrameOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Transparent)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.02f))
            )
        }
        Icon(
            Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = EthosPrimaryYellow.copy(alpha = 0.9f),
            modifier = Modifier.size(220.dp)
        )
    }
}

@Composable
private fun EstadoOverlay(mensaje: String, esError: Boolean = false, onReintentar: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(32.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!esError) CircularProgressIndicator(color = EthosGreen)
                Spacer(Modifier.height(12.dp))
                Text(mensaje, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Medium, color = EthosTextDark)
                if (esError && onReintentar != null) {
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onReintentar, colors = ButtonDefaults.buttonColors(containerColor = EthosGreen)) {
                        Text("Reintentar", color = Color.White) 
                    }
                }
            }
        }
    }
}

@Composable
private fun PermisoRequerido(onSolicitar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Necesitamos acceso a la cámara para escanear los puntos ecológicos", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSolicitar) { Text("Permitir cámara") }
    }
}
