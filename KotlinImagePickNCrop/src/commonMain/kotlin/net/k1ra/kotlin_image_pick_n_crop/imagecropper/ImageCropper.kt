package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile

sealed interface ImageCropResult {
    data class Success(val bitmap: ImageBitmap) : ImageCropResult
    object Cancelled : ImageCropResult
}

enum class ImageCropError : ImageCropResult {
    ImageLoadingError,
    ImageSavingError
}

enum class ImageCropperLoading {
    PreparingImage,
    SavingImageResult,
}

internal val DefaultMaxCropSize = IntSize(3000, 3000)

@Stable
interface ImageCropper {
    val imageCropState: ImageCropState?

    val imgLoadingStatus: ImageCropperLoading?

    suspend fun cropImage(
        maxResultSize: IntSize? = DefaultMaxCropSize,
        createSrc: suspend () -> ImgSrc?
    ): ImageCropResult
}

suspend fun ImageCropper.cropImage(
    maxResultSize: IntSize? = DefaultMaxCropSize,
    bmp: ImageBitmap
): ImageCropResult = cropImage(maxResultSize = maxResultSize) {
    ImgBitmapSrc(bmp)
}

@Composable
fun rememberImageCropper(): ImageCropper {
    return remember { ImageCropper() }
}

internal fun ImageCropper(): ImageCropper = object : ImageCropper {
    override var imageCropState: ImageCropState? by mutableStateOf(null)
    private val imgCropStateFlow = snapshotFlow { imageCropState }
    override var imgLoadingStatus: ImageCropperLoading? by mutableStateOf(null)
    override suspend fun cropImage(
        maxResultSize: IntSize?,
        createSrc: suspend () -> ImgSrc?
    ): ImageCropResult {
        imageCropState = null
        val src = withLoading(ImageCropperLoading.PreparingImage) { createSrc() }
            ?: return ImageCropError.ImageLoadingError
        val newCrop = ImageCropState(src) { imageCropState = null }
        imageCropState = newCrop
        imgCropStateFlow.takeWhile { it === newCrop }.collect()
        if (!newCrop.accepted) return ImageCropResult.Cancelled
        return withLoading(ImageCropperLoading.SavingImageResult) {
            val result = newCrop.generateResult(maxResultSize)
            if (result == null) ImageCropError.ImageSavingError
            else ImageCropResult.Success(result)
        }
    }

    inline fun <R> withLoading(status: ImageCropperLoading, op: () -> R): R {
        return try {
            imgLoadingStatus = status
            op()
        } finally {
            imgLoadingStatus = null
        }
    }
}