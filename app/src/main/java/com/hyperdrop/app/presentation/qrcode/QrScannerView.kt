package com.hyperdrop.app.presentation.qrcode

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

@Composable
fun QrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var lastScannedValue by remember { mutableStateOf<String?>(null) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    CameraPermissionWrapper(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(previewView) {
            val view = previewView ?: return@LaunchedEffect
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val barcodeScanner = BarcodeScanning.getClient(options)
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    MlKitAnalyzer(
                        listOf(barcodeScanner),
                        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                        cameraExecutor,
                        Consumer { result ->
                            val barcodeResults = result?.getValue(barcodeScanner)
                            if (!barcodeResults.isNullOrEmpty()) {
                                val value = barcodeResults.first().rawValue
                                if (value != null && value != lastScannedValue) {
                                    lastScannedValue = value
                                    onQrCodeScanned(value)
                                }
                            }
                        }
                    )
                )

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("QrScannerView", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }
}
