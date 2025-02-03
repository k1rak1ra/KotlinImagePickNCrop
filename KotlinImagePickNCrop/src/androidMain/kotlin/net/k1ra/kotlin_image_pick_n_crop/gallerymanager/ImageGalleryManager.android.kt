package net.k1ra.kotlin_image_pick_n_crop.gallerymanager

import android.content.ContentResolver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import net.k1ra.kotlin_image_pick_n_crop.gallerymanager.ImageGalleryManager
import net.k1ra.kotlin_image_pick_n_crop.utils.BitmapUtils
import net.k1ra.kotlin_image_pick_n_crop.utils.SharedImage

@Composable
actual fun rememberImageGalleryManager(onResult: (SharedImage?) -> Unit): ImageGalleryManager {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                onResult.invoke(
                    SharedImage(
                        BitmapUtils.getBitmapFromUri(
                            uri,
                            contentResolver
                        )
                    )
                )
            }
        }
    return remember {
        ImageGalleryManager(onLaunch = {
            galleryLauncher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        })
    }
}

actual class ImageGalleryManager actual constructor(private val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}