package com.quickqr.scanner.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/** ML Kit QR-only analyzer with a one-shot lock to avoid duplicate navigations. */
class QrCodeAnalyzer(
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val locked = AtomicBoolean(false)

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    fun reset() {
        locked.set(false)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (locked.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes
                    .asSequence()
                    .mapNotNull { it.rawValue }
                    .firstOrNull { it.isNotBlank() }
                if (value != null && locked.compareAndSet(false, true)) {
                    onQrDetected(value)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
