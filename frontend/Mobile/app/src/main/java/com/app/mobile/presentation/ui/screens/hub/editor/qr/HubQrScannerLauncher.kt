package com.app.mobile.presentation.ui.screens.hub.editor.qr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class HubQrScannerLauncher internal constructor(
    private val launch: (onRaw: (String?) -> Unit, onError: (Throwable) -> Unit, onCanceled: () -> Unit) -> Unit
) {
    fun scan(
        onResult: (HubQrParseResult) -> Unit,
        onError: (Throwable) -> Unit,
        onCanceled: () -> Unit = {}
    ) {
        launch(
            { raw -> onResult(HubQrParser.parse(raw)) },
            onError,
            onCanceled
        )
    }
}

@Composable
fun rememberHubQrScannerLauncher(): HubQrScannerLauncher {
    val context = LocalContext.current
    return remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        val scanner = GmsBarcodeScanning.getClient(context, options)

        HubQrScannerLauncher { onRaw, onError, onCanceled ->
            scanner.startScan()
                .addOnSuccessListener { barcode -> onRaw(barcode.rawValue) }
                .addOnCanceledListener { onCanceled() }
                .addOnFailureListener { e -> onError(e) }
        }
    }
}

