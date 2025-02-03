package net.k1ra.kotlin_image_pick_n_crop.gallerymanager

import androidx.compose.runtime.Composable
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage

@Composable
expect fun rememberImageGalleryManager(onResult: (SharedImage?) -> Unit): ImageGalleryManager

expect class ImageGalleryManager(
    onLaunch: () -> Unit
) {
    fun launch()
}