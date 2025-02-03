package net.k1ra.kotlin_image_pick_n_crop.utils

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream

object BitmapUtils {
    fun getBitmapFromUri(uri: Uri, contentResolver: ContentResolver): android.graphics.Bitmap? {
        val inputStream: InputStream?
        return try {
            inputStream = contentResolver.openInputStream(uri)
            val s = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            s
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}