package net.k1ra.kotlin_image_pick_n_crop.gallerymanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.loadImageBitmap
import java.io.FileInputStream
import kotlinx.coroutines.launch
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage

@Composable
actual fun rememberImageGalleryManager(onResult: (SharedImage?) -> Unit): ImageGalleryManager {
    val coroutineScope = rememberCoroutineScope()

    return remember {
        ImageGalleryManager(onLaunch = {
            coroutineScope.launch {
                PlatformFilePicker.current.pickFile(
                    title = "Select Image",
                    initialDirectory = null,
                    fileExtensions = listOf("png", "jpg", "jpeg"),
                    parentWindow = null,
                )?.let {
                    val imageBitmap = loadImageBitmap(FileInputStream(it))
                    onResult.invoke(SharedImage(imageBitmap))
                }
            }
        })
    }
}

actual class ImageGalleryManager actual constructor(val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}