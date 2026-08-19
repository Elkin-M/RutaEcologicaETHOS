package com.ethos.rutaecologica.ui.screens.result

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.webkit.WebViewAssetLoader
import coil.compose.AsyncImage
import com.ethos.rutaecologica.ui.common.EstrellasChip
import com.ethos.rutaecologica.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File

// Fondo fijo del recuadro del visor 3D: se ve SIEMPRE (cargando, error o
// modelo listo) para que quede claro dónde debe aparecer el modelo aunque
// el WebView esté transparente o en blanco.
private val ColorFondoVisor3D = Color(0xFFE7E2D3)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultScreen(
    onContinuar: (lugarId: String, estrellas: Int) -> Unit,
    onVolver: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val lugar by viewModel.lugar.collectAsState()
    val reproduciendo by viewModel.isPlaying.collectAsState()
    val modelo3DLocal by viewModel.modelo3DLocal.collectAsState()
    val modelo3DCargando by viewModel.modelo3DCargando.collectAsState()
    val modelo3DError by viewModel.modelo3DError.collectAsState()
    val modelo3DProgreso by viewModel.modelo3DProgreso.collectAsState()

    var visorFullScreen by remember { mutableStateOf(false) }

    val datos = lugar ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(EthosBackground)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: video o imagen, ahora despegado del borde de la
            // pantalla y respetando el status bar (donde están las
            // notificaciones), con márgenes y esquinas redondeadas.
            val mediaItems = remember(datos) {
                val list = mutableListOf<String>()
                if (datos.imagen.isNotBlank()) list.add("IMG")
                if (datos.video.isNotBlank()) list.add("VID")
                list
            }
            val pagerState = rememberPagerState(pageCount = { mediaItems.size })

            if (mediaItems.size > 1) {
                LaunchedEffect(pagerState) {
                    delay(5000)
                    if (pagerState.currentPage == 0) {
                        pagerState.animateScrollToPage(1)
                    }
                }
            }

            // Estado para que el contenedor adapte su altura según la imagen/video
            var mediaAspectRatio by remember { mutableFloatStateOf(1.5f) } 

            Box(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize() // Transición suave al cambiar de tamaño
                        .aspectRatio(mediaAspectRatio) 
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                ) { page ->
                    when (mediaItems[page]) {
                        "IMG" -> {
                            AsyncImage(
                                model = datos.imagen,
                                contentDescription = datos.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit, 
                                onSuccess = { state ->
                                    val size = state.painter.intrinsicSize
                                    if (size.width > 0 && size.height > 0) {
                                        // Ajustar ratio dinámicamente (limite para no romper el UI)
                                        mediaAspectRatio = (size.width / size.height).coerceIn(0.6f, 2.0f)
                                    }
                                }
                            )
                        }
                        "VID" -> {
                            VideoHeader(
                                url = datos.video,
                                modifier = Modifier.fillMaxSize(),
                                onVideoSizeChanged = { ratio ->
                                    mediaAspectRatio = ratio.coerceIn(0.6f, 2.0f)
                                }
                            )
                        }
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (datos.categoria.isNotBlank()) {
                    AssistChip(onClick = {}, label = { Text(datos.categoria) })
                    Spacer(Modifier.height(10.dp))
                }

                Text(
                    datos.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    color = EthosTextDark
                )
                Spacer(Modifier.height(10.dp))
                EstrellasChip(cantidad = datos.estrellas)
                Spacer(Modifier.height(18.dp))

                Text(
                    datos.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = EthosTextDark.copy(alpha = 0.85f)
                )

                Spacer(Modifier.height(20.dp))

                if (datos.audio.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.playPauseAudio(datos.audio) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            if (reproduciendo) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (reproduciendo) "Pausar audioguía" else "Escuchar audioguía")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Visor 3D: solo aparece si el lugar tiene modelo3D cargado.
                // El recuadro de 280dp con fondo ColorFondoVisor3D se muestra
                // SIEMPRE en los 3 estados (descargando / error / listo), así
                // nunca queda un espacio en blanco sin explicación.
                if (datos.modelo3D.isNotBlank()) {
                    Text(
                        "Modelo 3D",
                        style = MaterialTheme.typography.titleMedium,
                        color = EthosTextDark
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ColorFondoVisor3D),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            modelo3DCargando -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                ) {
                                    CircularProgressIndicator(color = EthosGreen)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Descargando modelo 3D...",
                                        color = EthosTextDark.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(10.dp))

                                    if (modelo3DProgreso < 0f) {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(50)),
                                            color = EthosGreen,
                                            trackColor = EthosTextDark.copy(alpha = 0.15f)
                                        )
                                    } else {
                                        LinearProgressIndicator(
                                            progress = { modelo3DProgreso },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(50)),
                                            color = EthosGreen,
                                            trackColor = EthosGreenDark.copy(alpha = 0.1f)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "${(modelo3DProgreso * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EthosTextDark.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            modelo3DError != null -> {
                                MensajeErrorVisor(
                                    titulo = "Error al descargar el modelo",
                                    detalle = modelo3DError ?: ""
                                )
                            }
                            modelo3DLocal != null -> {
                                Box(Modifier.fillMaxSize()) {
                                    Model3DViewer(
                                        archivoLocal = modelo3DLocal!!,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Overlay para evitar que el WebView capture el scroll del
                                    // contenedor principal y permitir expandir a pantalla completa.
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .clickable { visorFullScreen = true }
                                            .background(Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Fullscreen,
                                            contentDescription = "Expandir",
                                            tint = EthosGreenDark.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onContinuar(datos.id, datos.estrellas) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EthosGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Botón "volver": overlay fijo, ahora también respeta el status bar
        // para no quedar debajo de las notificaciones.
        IconButton(
            onClick = onVolver,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }

        // Visor FullScreen: se muestra sobre todo lo demás cuando se activa.
        if (visorFullScreen && modelo3DLocal != null) {
            BackHandler { visorFullScreen = false }
            Visor3DFullScreen(
                archivoLocal = modelo3DLocal!!,
                nombre = datos.nombre,
                descripcion = datos.descripcion,
                onCerrar = { visorFullScreen = false }
            )
        }
    }
}

/**
 * Mensaje de error mostrado dentro del propio recuadro del visor 3D, para
 * poder diagnosticar sin logcat qué está fallando.
 */
@Composable
private fun MensajeErrorVisor(titulo: String, detalle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(16.dp)
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodySmall,
            color = EthosTextDark.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Header de video usando Media3/ExoPlayer.
 */
@Composable
private fun VideoHeader(
    url: String, 
    modifier: Modifier = Modifier,
    onVideoSizeChanged: (Float) -> Unit = {}
) {
    val context = LocalContext.current

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    onVideoSizeChanged(videoSize.width.toFloat() / videoSize.height.toFloat())
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { 
            exoPlayer.removeListener(listener)
            exoPlayer.release() 
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Estado interno de la carga del visor 3D dentro del WebView (distinto del
 * estado de descarga del archivo, que ya se maneja en el ViewModel).
 */
private enum class EstadoWebView { CARGANDO, LISTO, ERROR }

/**
 * Puente JS -> Android para saber si <model-viewer> realmente terminó de
 * renderizar el modelo o si tiró un error, ya que sin esto el WebView puede
 * quedar en blanco sin ningún aviso visible.
 *
 * Los métodos @JavascriptInterface se ejecutan en un hilo interno del
 * WebView, por eso se despachan al hilo principal antes de tocar el estado
 * de Compose.
 */
private class ModelViewerBridge(
    private val onLoad: () -> Unit,
    private val onError: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onModelLoad() {
        handler.post { onLoad() }
    }

    @JavascriptInterface
    fun onModelError(mensaje: String) {
        handler.post { onError(mensaje) }
    }
}

/**
 * Visor de modelos 3D en formato .glb usando el web component
 * <model-viewer> de Google dentro de un WebView.
 *
 * Recibe [archivoLocal], ya descargado por Model3DDownloader, y lo sirve al
 * WebView mediante WebViewAssetLoader bajo el dominio virtual
 * https://appassets.androidplatform.net/.
 *
 * Diagnóstico sin logcat: se agrega un puente JS ([ModelViewerBridge]) que
 * escucha los eventos 'load' y 'error' del propio <model-viewer>, más un
 * timeout de 10s que detecta si la librería model-viewer nunca llegó a
 * registrarse (por ejemplo si no hay acceso a unpkg.com) y errores de red
 * del propio WebViewClient. Cualquiera de estos casos se muestra como texto
 * legible en el recuadro donde debería estar el modelo.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Model3DViewer(archivoLocal: File, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var estado by remember(archivoLocal) { mutableStateOf(EstadoWebView.CARGANDO) }
    var mensajeError by remember(archivoLocal) { mutableStateOf("") }

    val assetLoader = remember(archivoLocal) {
        WebViewAssetLoader.Builder()
            .addPathHandler(
                "/modelos3d/",
                WebViewAssetLoader.InternalStoragePathHandler(
                    context,
                    archivoLocal.parentFile ?: archivoLocal
                )
            )
            .build()
    }

    val bridge = remember(archivoLocal) {
        ModelViewerBridge(
            onLoad = {
                estado = EstadoWebView.LISTO
            },
            onError = { mensaje ->
                estado = EstadoWebView.ERROR
                mensajeError = mensaje
            }
        )
    }

    // Si a los 10s ni 'load' ni 'error' se dispararon, lo más probable es
    // que la librería model-viewer nunca haya cargado (sin internet a
    // unpkg.com, bloqueada por firewall/proxy, etc.) — en ese caso
    // <model-viewer> queda como una etiqueta vacía y nunca dispara eventos.
    LaunchedEffect(archivoLocal) {
        delay(10_000)
        if (estado == EstadoWebView.CARGANDO) {
            estado = EstadoWebView.ERROR
            mensajeError = "Tiempo de espera agotado cargando el visor 3D. " +
                    "Es probable que no haya conexión a internet para descargar " +
                    "la librería model-viewer (unpkg.com), o que el archivo " +
                    ".glb esté dañado."
        }
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // 1. CONFIGURACIÓN DEL WEBVIEW (Rendimiento)
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = false
                        
                        // Prioridad de renderizado (Solo disponible vía setRenderPriority en versiones antiguas)
                        @Suppress("DEPRECATION")
                        setRenderPriority(WebSettings.RenderPriority.HIGH)
                        
                        // Configuración de Cache para evitar re-decodificación innecesaria
                        cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                        domStorageEnabled = true
                    }
                    
                    setBackgroundColor(0x00000000)

                    addJavascriptInterface(bridge, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            return assetLoader.shouldInterceptRequest(request.url)
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            super.onReceivedError(view, request, error)
                            // Solo nos importa si falla el documento principal,
                            // no subrecursos sueltos.
                            if (request.isForMainFrame) {
                                estado = EstadoWebView.ERROR
                                mensajeError = "Error de red cargando el visor: " +
                                        "${error.description} (${request.url})"
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                            // Si algo truena en la consola JS antes de que el
                            // puente pueda reportarlo (p.ej. error de sintaxis
                            // en un script de terceros), lo dejamos como pista.
                            if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR &&
                                estado == EstadoWebView.CARGANDO
                            ) {
                                mensajeError = "Consola JS: ${consoleMessage.message()}"
                            }
                            return true
                        }
                    }
                }
            },
            update = { webView ->
                val nombreArchivo = archivoLocal.name
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta name="viewport" content="width=device-width, initial-scale=1.0">
                      <style>
                        html, body { margin:0; padding:0; height:100%; background:transparent; }
                        model-viewer { width:100%; height:100%; --poster-color: transparent; }
                      </style>
                    </head>
                    <body>
                    <model-viewer
                        id="mv"
                        src="https://appassets.androidplatform.net/modelos3d/$nombreArchivo"
                        camera-controls
                        auto-rotate
                        shadow-intensity="1"
                        exposure="1"
                        loading="eager"
                        reveal="manual"
                        interpolation-decay="20"
                        min-camera-orbit="auto auto 5%"
                        max-camera-orbit="auto auto 200%"
                        style="width:100%;height:100%; opacity: 0; transition: opacity 0.3s ease-in-out;">
                      </model-viewer>

                      <script>
                        window.onerror = function(msg, url, line) {
                          if (window.AndroidBridge) {
                            AndroidBridge.onModelError('Error de script: ' + msg + ' (' + url + ':' + line + ')');
                          }
                        };
                      </script>
                      <script type="module" src="https://unpkg.com/@google/model-viewer@3.5.0/dist/model-viewer.min.js"></script>
                      <script>
                        var mv = document.getElementById('mv');
                        
                        // Si a los 8s el custom element nunca se registró
                        setTimeout(function () {
                          if (!customElements.get('model-viewer')) {
                            if (window.AndroidBridge) {
                              AndroidBridge.onModelError('La librería model-viewer no se pudo cargar.');
                            }
                          }
                        }, 8000);

                        mv.addEventListener('load', function () {
                          // Revelar manualmente para evitar "pop-in"
                          mv.style.opacity = '1';
                          if (typeof mv.dismissPoster === 'function') {
                            mv.dismissPoster();
                          }
                          
                          if (window.AndroidBridge) AndroidBridge.onModelLoad();
                        });

                        // Optimización: Pausar auto-rotate cuando no está en pantalla o hay interacción
                        mv.addEventListener('camera-change', function() {
                           // El usuario está moviendo la cámara, model-viewer pausa auto-rotate solo
                        });
                        mv.addEventListener('error', function (e) {
                          // OJO: JSON.stringify(new Error(...)) da "{}" porque
                          // 'message' y 'stack' no son propiedades enumerables.
                          // Por eso extraemos el motivo a mano en vez de
                          // stringificar todo el detail de una.
                          var detail = (e && e.detail) || {};
                          var tipo = detail.type || 'desconocido';
                          var motivo = 'sin detalle adicional';
                          var se = detail.sourceError;
                          if (se) {
                            if (se.message) { motivo = se.message; }
                            else if (typeof se.toString === 'function') { motivo = se.toString(); }
                          }
                          if (window.AndroidBridge) {
                            AndroidBridge.onModelError('El visor no pudo renderizar el archivo .glb (tipo: ' + tipo + '). Motivo: ' + motivo);
                          }
                        });
                      </script>
                    </body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        )

        // Overlay de carga: se ve encima del WebView (que puede estar en
        // blanco mientras tanto) hasta que 'load' confirme que el modelo
        // ya se renderizó.
        if (estado == EstadoWebView.CARGANDO) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = EthosGreen)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cargando visor 3D...",
                    color = EthosTextDark.copy(alpha = 0.7f)
                )
            }
        }

        // Overlay de error: reemplaza el WebView en blanco por un mensaje
        // legible, sin necesidad de logcat.
        if (estado == EstadoWebView.ERROR) {
            MensajeErrorVisor(
                titulo = "No se pudo mostrar el modelo 3D",
                detalle = mensajeError
            )
        }
    }
}

/**
 * Vista a pantalla completa del modelo 3D para evitar conflictos con el
 * scroll de la pantalla principal y permitir una mejor interacción.
 */
@Composable
private fun Visor3DFullScreen(
    archivoLocal: File,
    nombre: String,
    descripcion: String,
    onCerrar: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Model3DViewer(
            archivoLocal = archivoLocal,
            modifier = Modifier.fillMaxSize()
        )

        // Botón cerrar (arriba a la derecha)
        IconButton(
            onClick = onCerrar,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        // Info abajo (Nombre + Descripción)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = 350.dp) 
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.3f to Color.Black.copy(alpha = 0.8f),
                        1f to Color.Black
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}