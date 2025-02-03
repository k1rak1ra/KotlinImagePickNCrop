package net.k1ra.kotlin_image_pick_n_crop.cameramanager

import PlatformFilePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.loadImageBitmap
import kotlinx.coroutines.launch
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage
import java.io.FileInputStream

@Composable
actual fun rememberImageCaptureManager(onResult: (SharedImage?) -> Unit): ImageCaptureManager {
    val coroutineScope = rememberCoroutineScope()

    return remember {
        ImageCaptureManager(onLaunch = {
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

actual class ImageCaptureManager actual constructor(val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}