package net.k1ra.kotlin_image_pick_n_crop.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

actual class SharedImage(private val bitmap: android.graphics.Bitmap?) {
    actual fun toByteArray(): ByteArray? {
        return if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            @Suppress("MagicNumber") bitmap.compress(
                android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream
            )
            val byteArray = byteArrayOutputStream.toByteArray()
            byteArray
        } else {
            null
        }
    }

    actual fun toImageBitmap(): ImageBitmap? {
//        val byteArray = toByteArray()
//        return if (byteArray != null) {
//            val imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
//            return imageBitmap
//        } else {
//            null
//        }
        return bitmap?.asImageBitmap()
    }
}