package net.k1ra.kotlin_image_pick_n_crop.gallerymanager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.k1ra.kotlin_image_pick_n_crop.gallerymanager.ImageGalleryManager
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume

//New code to implement wasm file picker (c) k1rak1ra 2025
@Composable
actual fun rememberImageGalleryManager(onResult: (SharedImage?) -> Unit): ImageGalleryManager {
    val coroutineScope = rememberCoroutineScope()

    return remember {
        ImageGalleryManager(onLaunch = {
            coroutineScope.launch {
                val input = document.createElement("input") as HTMLInputElement
                input.style.display = "none"
                document.body?.appendChild(input)

                input.apply {
                    this.type = "file"
                    accept = "image/*"
                    multiple = false
                }

                input.onchange = { event ->
                    // Get the selected files
                    val file = event.target?.unsafeCast<HTMLInputElement>()?.files?.asList()?.firstOrNull()

                    if (file != null) {
                        CoroutineScope(Dispatchers.Default).launch {
                            val imageBitmap = file.toByteArray()?.toImageBitmap()
                            onResult.invoke(SharedImage(imageBitmap))
                        }
                    }

                    document.body?.removeChild(input)
                }

                input.oncancel = {
                    document.body?.removeChild(input)
                }

                input.click()
            }
        })
    }
}

actual class ImageGalleryManager actual constructor(private val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}

//New code to implement wasm file picker (c) k1rak1ra 2025
suspend fun File.toByteArray() : ByteArray? = suspendCancellableCoroutine { continuation ->
    val reader = FileReader()

    reader.onload = { event ->
        val arrayBuffer = event.target?.unsafeCast<FileReader>()?.result?.unsafeCast<ArrayBuffer>()

        if (arrayBuffer != null)
            continuation.resume(Uint8Array(arrayBuffer).asByteArray())
        else
            continuation.resume(null)
    }

    reader.readAsArrayBuffer(this)
}

//New code to implement wasm file picker (c) k1rak1ra 2025
fun Uint8Array.asByteArray(): ByteArray = ByteArray(length) { this[it] }

//New code to implement wasm file picker (c) k1rak1ra 2025
fun ByteArray.toImageBitmap(): ImageBitmap = Image.makeFromEncoded(this).toComposeImageBitmap()