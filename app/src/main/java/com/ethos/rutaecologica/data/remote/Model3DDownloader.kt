package com.ethos.rutaecologica.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resultado de intentar obtener el modelo 3D local. A diferencia de un
 * simple `File?`, esto permite devolver SIEMPRE un mensaje de error
 * específico y entendible (sin necesitar logcat) sobre qué falló:
 * red, Drive, o archivo corrupto/no válido.
 */
sealed class ResultadoModelo3D {
    data class Exito(val archivo: File) : ResultadoModelo3D()
    data class Error(val mensaje: String) : ResultadoModelo3D()
}

/**
 * Descarga archivos .glb (modelos 3D) desde una URL de Google Drive y los
 * guarda en el almacenamiento interno de la app, para que el WebView del
 * visor 3D los pueda leer localmente en vez de pedirle a Drive la URL
 * directa (que para archivos grandes devuelve una página HTML de
 * advertencia "no se puede escanear por virus" en lugar del archivo).
 *
 * Cómo funciona el "bypass" de esa advertencia:
 * 1. Pide la URL de descarga normal (uc?export=download&id=...).
 * 2. Si Drive responde con la página de advertencia (detectable porque el
 *    Content-Type es text/html en vez de binario), busca dentro del HTML
 *    el token "confirm=" y la cookie de sesión que Drive exige.
 * 3. Repite la petición agregando ese token + cookie, lo que hace que
 *    Drive esta vez sí entregue el archivo real.
 *
 * Validación de integridad:
 * Ese bypass puede fallar de formas silenciosas (Drive cambia su flujo de
 * confirmación, el archivo no es realmente público, cuota excedida, etc.)
 * y terminar guardando algo que NO es un .glb real aunque la descarga
 * "técnicamente" haya funcionado. Por eso, después de escribir el archivo
 * se valida la firma binaria (magic number) de un GLB válido: los primeros
 * 4 bytes deben ser ASCII "glTF" (0x67 0x6C 0x54 0x46). Si no coinciden, se
 * borra el archivo y se reporta un error específico en vez de dejar pasar
 * un archivo corrupto al visor (que ahí se manifestaría como una pantalla
 * en blanco o un error genérico "loadFailure" sin detalle).
 *
 * Progreso de descarga:
 * [obtenerModeloLocal] acepta un callback [onProgress] que se invoca
 * periódicamente mientras se copia el archivo, con un valor entre 0f y 1f.
 * Si Drive no informa el tamaño total del archivo (Content-Length ausente
 * o -1), se emite -1f una sola vez para indicar "progreso indeterminado".
 */
@Singleton
class Model3DDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "Model3DDownloader"
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val BUFFER_SIZE = 8 * 1024

        // Firma binaria ("magic number") de un archivo GLB válido: los
        // primeros 4 bytes son siempre los caracteres ASCII "glTF".
        private val FIRMA_GLB = byteArrayOf(0x67, 0x6C, 0x54, 0x46) // "g","l","T","F"
    }

    private val carpetaModelos: File by lazy {
        File(context.filesDir, "modelos3d").apply { mkdirs() }
    }

    /**
     * Descarga (o reutiliza si ya está en caché) el .glb correspondiente a
     * [urlDrive]. Devuelve [ResultadoModelo3D.Exito] con el File local
     * listo para usar, o [ResultadoModelo3D.Error] con un mensaje
     * específico de qué falló.
     *
     * @param onProgress callback opcional invocado con el progreso de la
     * descarga (0f..1f), o -1f si el tamaño total es desconocido. Si el
     * archivo ya estaba en caché y es válido, se llama una vez con 1f y no
     * se descarga nada.
     */
    suspend fun obtenerModeloLocal(
        urlDrive: String,
        onProgress: (Float) -> Unit = {}
    ): ResultadoModelo3D = withContext(Dispatchers.IO) {
        val fileId = extraerFileId(urlDrive)
        if (fileId == null) {
            Log.e(TAG, "No se pudo extraer el ID de Drive de: $urlDrive")
            return@withContext ResultadoModelo3D.Error(
                "El link de Google Drive no tiene un formato reconocido. " +
                        "Debe ser del tipo .../file/d/ID/view o uc?id=ID."
            )
        }

        val destino = File(carpetaModelos, "$fileId.glb")

        // Cache: si ya lo descargamos antes, lo validamos antes de reusarlo.
        // Si por algún motivo el archivo cacheado quedó corrupto de una
        // corrida anterior, lo borramos y volvemos a descargar en vez de
        // fallar en silencio otra vez.
        if (destino.exists() && destino.length() > 0) {
            if (tieneFirmaGlbValida(destino)) {
                Log.i(TAG, "✅ Modelo ya en caché local: ${destino.absolutePath} (${destino.length()} bytes)")
                onProgress(1f)
                return@withContext ResultadoModelo3D.Exito(destino)
            } else {
                Log.w(TAG, "⚠️ Archivo en caché no es un GLB válido, se borra y se vuelve a descargar")
                destino.delete()
            }
        }

        Log.i(TAG, "⬇️ Descargando modelo 3D, fileId=$fileId ...")
        val resultado = try {
            descargarConBypassDeDrive(fileId, destino, onProgress)
        } catch (e: IOException) {
            Log.e(TAG, "Excepción de red descargando el modelo 3D", e)
            destino.delete()
            ResultadoModelo3D.Error("Error de conexión al descargar el modelo: ${e.message ?: e::class.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Excepción inesperada descargando el modelo 3D", e)
            destino.delete()
            ResultadoModelo3D.Error("Error inesperado al descargar el modelo: ${e.message ?: e::class.simpleName}")
        }

        when (resultado) {
            is ResultadoModelo3D.Exito ->
                Log.i(TAG, "✅ Modelo 3D descargado y validado: ${destino.absolutePath} (${destino.length()} bytes)")
            is ResultadoModelo3D.Error ->
                Log.e(TAG, "❌ Falló la descarga del modelo 3D (fileId=$fileId): ${resultado.mensaje}")
        }
        resultado
    }

    private fun extraerFileId(url: String): String? {
        if (url.isBlank()) return null
        val uri = Uri.parse(url)

        // Formato: https://drive.google.com/uc?export=download&id=FILE_ID
        uri.getQueryParameter("id")?.let { return it }

        // Formato: https://drive.google.com/file/d/FILE_ID/view...
        val segmentos = uri.pathSegments
        val idxD = segmentos.indexOf("d")
        if (idxD != -1 && idxD + 1 < segmentos.size) {
            return segmentos[idxD + 1]
        }

        return null
    }

    private fun descargarConBypassDeDrive(
        fileId: String,
        destino: File,
        onProgress: (Float) -> Unit
    ): ResultadoModelo3D {
        val urlBase = "https://drive.google.com/uc?export=download&id=$fileId"

        var conn = abrirConexion(urlBase, cookie = null)
        var contentType = conn.contentType ?: ""

        // Si Drive devolvió HTML, es la página de advertencia de archivo grande.
        if (contentType.contains("text/html")) {
            Log.w(TAG, "Drive devolvió página de advertencia, buscando token de confirmación...")

            val cookie = conn.headerFields["Set-Cookie"]?.joinToString("; ") { it.substringBefore(";") }
            val html = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val confirmToken = Regex("confirm=([0-9A-Za-z_]+)").find(html)?.groupValues?.get(1)

            if (confirmToken == null) {
                Log.e(TAG, "No se encontró token de confirmación en la página de Drive.")
                return ResultadoModelo3D.Error(
                    "Google Drive no entregó un token de confirmación para la descarga. " +
                            "Es posible que el archivo no sea público (revisa que el link tenga " +
                            "permiso 'Cualquiera con el enlace') o que Google haya cambiado el " +
                            "flujo de descarga."
                )
            }

            val urlConfirmada = "https://drive.google.com/uc?export=download&confirm=$confirmToken&id=$fileId"
            conn = abrirConexion(urlConfirmada, cookie)
            contentType = conn.contentType ?: ""

            if (contentType.contains("text/html")) {
                Log.e(TAG, "Segundo intento también devolvió HTML.")
                conn.disconnect()
                return ResultadoModelo3D.Error(
                    "Google Drive devolvió una página de advertencia incluso después de " +
                            "confirmar la descarga. El archivo podría haber excedido la cuota de " +
                            "descargas de Drive por hoy, o el link cambió de formato."
                )
            }
        }

        return try {
            copiarConProgreso(conn, destino, onProgress)

            if (!tieneFirmaGlbValida(destino)) {
                Log.e(TAG, "El archivo descargado no tiene la firma binaria de un GLB válido")
                destino.delete()
                return ResultadoModelo3D.Error(
                    "El archivo descargado no es un modelo .glb válido (la firma binaria no " +
                            "coincide). Lo más probable es que Google Drive haya bloqueado la " +
                            "descarga automática y haya entregado otra cosa en vez del binario, o " +
                            "que el link no apunte realmente a un archivo .glb."
                )
            }

            ResultadoModelo3D.Exito(destino)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción escribiendo el archivo local", e)
            destino.delete()
            ResultadoModelo3D.Error("Error guardando el archivo descargado: ${e.message ?: e::class.simpleName}")
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Revisa si [archivo] comienza con la firma binaria "glTF" que deben
     * tener todos los archivos .glb válidos (formato binario de glTF).
     */
    private fun tieneFirmaGlbValida(archivo: File): Boolean {
        if (!archivo.exists() || archivo.length() < 4) return false
        return try {
            archivo.inputStream().use { input ->
                val cabecera = ByteArray(4)
                val leidos = input.read(cabecera)
                leidos == 4 && cabecera.contentEquals(FIRMA_GLB)
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo leer la cabecera del archivo para validar firma GLB", e)
            false
        }
    }

    /**
     * Copia el body de [conn] a [destino] leyendo en bloques, y va
     * reportando el progreso a [onProgress]. Si no se conoce el tamaño
     * total (contentLengthLong <= 0), emite -1f una sola vez al inicio.
     */
    private fun copiarConProgreso(
        conn: HttpURLConnection,
        destino: File,
        onProgress: (Float) -> Unit
    ) {
        val totalBytes = conn.contentLengthLong // -1 si no viene el header
        if (totalBytes <= 0) {
            onProgress(-1f)
        } else {
            onProgress(0f)
        }

        var bytesLeidos = 0L
        var ultimoPorcentajeReportado = -1

        conn.inputStream.use { input ->
            destino.outputStream().use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var leidos: Int
                while (input.read(buffer).also { leidos = it } != -1) {
                    output.write(buffer, 0, leidos)
                    bytesLeidos += leidos

                    if (totalBytes > 0) {
                        val porcentaje = ((bytesLeidos * 100) / totalBytes).toInt()
                        // Solo notificamos cuando cambia el % entero, para no
                        // saturar el StateFlow con actualizaciones inútiles.
                        if (porcentaje != ultimoPorcentajeReportado) {
                            ultimoPorcentajeReportado = porcentaje
                            onProgress(porcentaje / 100f)
                        }
                    }
                }
            }
        }

        onProgress(1f)
    }

    private fun abrirConexion(url: String, cookie: String?): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = true
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.setRequestProperty("User-Agent", USER_AGENT)
        if (cookie != null) {
            conn.setRequestProperty("Cookie", cookie)
        }
        conn.connect()
        return conn
    }
}