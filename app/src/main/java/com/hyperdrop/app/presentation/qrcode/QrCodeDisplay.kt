package com.hyperdrop.app.presentation.qrcode

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QrCodeDisplay(
    payload: String,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(true) }

    LaunchedEffect(payload) {
        isGenerating = true
        qrBitmap = QrCodeGenerator.generate(payload)
        isGenerating = false
    }

    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (isGenerating) {
            CircularProgressIndicator()
        } else {
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
