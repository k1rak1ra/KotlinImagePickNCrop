package net.k1ra.kotlin_image_pick_n_crop.utils

import androidx.compose.ui.graphics.ImageBitmap
import java.io.ByteArrayOutputStream

actual class SharedImage(private val imageBitmap: ImageBitmap?) {
    actual fun toByteArray(): ByteArray? {
        return if (imageBitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.toByteArray()
        } else {
            null
        }
    }

    actual fun toImageBitmap(): ImageBitmap? {
        return imageBitmap
    }
}