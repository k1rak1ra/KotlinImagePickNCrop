package net.k1ra.kotlin_image_pick_n_crop.cameramanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.k1ra.kotlin_image_pick_n_crop.cameramanager.ImageCaptureManager
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage

@Composable
actual fun rememberImageCaptureManager(onResult: (SharedImage?) -> Unit): ImageCaptureManager {
    return remember {
        ImageCaptureManager(onLaunch = {

        })
    }
}

actual class ImageCaptureManager actual constructor(val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}