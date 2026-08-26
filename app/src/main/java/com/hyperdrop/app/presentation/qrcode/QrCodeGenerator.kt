package com.hyperdrop.app.presentation.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrCodeGenerator {
    suspend fun generate(payload: String, size: Int = 512): Bitmap? = withContext(Dispatchers.Default) {
        if (payload.isBlank()) return@withContext null
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
