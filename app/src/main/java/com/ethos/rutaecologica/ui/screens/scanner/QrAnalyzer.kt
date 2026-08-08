package com.ethos.rutaecologica.ui.screens.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Reemplaza el componente `BarcodeScanner` de App Inventor.
 * Analiza cada frame de CameraX buscando un código QR y entrega el texto leído.
 */
class QrAnalyzer(
    private val onQrDetectado: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { it.valueType == Barcode.TYPE_TEXT || it.rawValue != null }
                    ?.rawValue?.let { valor -> onQrDetectado(valor) }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
