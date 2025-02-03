package net.k1ra.kotlin_image_pick_n_crop.cameramanager

import androidx.compose.runtime.Composable
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage

@Composable
expect fun rememberImageCaptureManager(onResult: (SharedImage?) -> Unit): ImageCaptureManager

expect class ImageCaptureManager(
    onLaunch: () -> Unit
) {
    fun launch()
}