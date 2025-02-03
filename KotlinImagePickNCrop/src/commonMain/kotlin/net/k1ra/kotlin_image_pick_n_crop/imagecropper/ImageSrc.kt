package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

data class DecodingParameters(val sampleSize: Int, val subset: IntRect)
data class DecodingResult(val params: DecodingParameters, val bmp: ImageBitmap)

internal fun computeScaledSize(imgRegion: IntSize, view: IntSize): Int {
    val imgArea = imgRegion.let { it.width.toDouble() * it.height }
    val viewArea = view.let { it.width.toDouble() * it.height }
    return (imgArea / viewArea).toFloat().align(2).coerceIn(1f, 32f).toInt()
}

private fun extractImagePortion(
    view: IntSize, viewToImg: Matrix, imgRect: IntRect, align: Boolean
): IntRect {
    return viewToImg
        .map(view.toSize().toRect()).let { if (align) it.align(128) else it }
        .roundOut().intersect(imgRect)
}

internal fun deriveDecodeParameters(
    view: IntSize,
    img: IntSize,
    imgToView: Matrix
): DecodingParameters? {
    if (view.width <= 0 || view.height <= 0) return null
    val imgRect = img.toIntRect()
    val viewToImg = imgToView.inverted()
    val subset = extractImagePortion(view, viewToImg, imgRect, align = true)
    if (subset.isEmpty) return null
    val sampleSize = computeScaledSize(
        imgRegion = extractImagePortion(view, viewToImg, imgRect, align = false).size,
        view = view
    )
    return DecodingParameters(sampleSize, subset)
}

internal fun DecodingParameters.includes(other: DecodingParameters): Boolean {
    return sampleSize == other.sampleSize &&
            subset.containsInclusive(other.subset)
}

@Composable
internal fun rememberLoadedMedia(src: ImgSrc, view: IntSize, imgToView: Matrix): DecodingResult? {
    var full by remember { mutableStateOf<DecodingResult?>(null) }
    var enhanced by remember { mutableStateOf<DecodingResult?>(null) }
    LaunchedEffect(src, view) {
        val fullMat = Matrix().apply {
            val imgRect = src.size.toSize().toRect()
            setRectToRect(imgRect, imgRect.fitIn(view.toSize().toRect()))
        }
        val fullParams = deriveDecodeParameters(view, src.size, fullMat)
        if (fullParams != null) full = src.open(fullParams)
    }
    LaunchedEffect(src, view, imgToView, full == null) decode@{
        if (full == null) return@decode
        if (enhanced == null) yield()
        val params = deriveDecodeParameters(view, src.size, imgToView) ?: return@decode
        if (enhanced?.params?.includes(params) == true) return@decode
        if (full?.params?.includes(params) == true) {
            enhanced = full
            return@decode
        }
        enhanced = null
        delay(500)
        enhanced = src.open(params)
    }
    return enhanced ?: full
}

@Stable
interface ImgSrc {
    val size: IntSize
    suspend fun open(params: DecodingParameters): DecodingResult?
}

internal data class ImgBitmapSrc(private val data: ImageBitmap) : ImgSrc {
    override val size: IntSize = IntSize(data.width, data.height)
    private val resultParams = DecodingParameters(1, size.toIntRect())
    override suspend fun open(params: DecodingParameters) = DecodingResult(resultParams, data)
}