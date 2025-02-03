package net.k1ra.kotlin_image_pick_n_crop.utils

import androidx.compose.ui.graphics.ImageBitmap

actual class SharedImage(private val imageBitmap: ImageBitmap?) {
    actual fun toByteArray(): ByteArray? {
        return null
    }

    actual fun toImageBitmap(): ImageBitmap? {
        return imageBitmap
    }
}