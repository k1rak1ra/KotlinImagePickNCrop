package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ImageAspectRatio(val x: Int, val y: Int)

internal data class ImageCropperStyleGuidelines(
    val count: Int = 2,
    val color: Color = Color.White,
    val width: Dp = .7f.dp,
)

@Stable
internal interface ImageCropperStyle {
    val backgroundColor: Color

    val overlayColor: Color

    fun DrawScope.drawCropRect(region: Rect)

    val handles: List<Offset>

    val touchRad: Dp

    val shapes: List<ImageCropShape>?

    val aspects: List<ImageAspectRatio>?

    val autoZoom: Boolean
    val enableRotationOption: Boolean
    val enabledFlipOption: Boolean
}

internal val SimpleImageCropperStyle: ImageCropperStyle by lazy { ImageCropperStyle() }

internal val LocalImageCropperStyle = staticCompositionLocalOf { SimpleImageCropperStyle }

private val MainImageHandles = listOf(
    Offset(0f, 0f), Offset(1f, 1f),
    Offset(1f, 0f), Offset(0f, 1f)
)

private val SecondaryImageHandles = listOf(
    Offset(.5f, 0f), Offset(1f, .5f),
    Offset(.5f, 1f), Offset(0f, .5f)
)

private val AllImageHandles = MainImageHandles + SecondaryImageHandles

internal val DefaultImageCropperAspectRatios = listOf(
    ImageAspectRatio(1, 1),
    ImageAspectRatio(16, 9),
    ImageAspectRatio(4, 3)
)

internal fun ImageCropperStyle(
    backgroundColor: Color = Color.Black,
    rectColor: Color = Color.White,
    rectStrokeWidth: Dp = 2.dp,
    touchRad: Dp = 20.dp,
    enableRotationOption: Boolean = true,
    enabledFlipOption: Boolean = true,
    guidelines: ImageCropperStyleGuidelines? = ImageCropperStyleGuidelines(),
    secondaryHandles: Boolean = true,
    overlay: Color = Color.Black.copy(alpha = .5f),
    shapes: List<ImageCropShape>? = DefaultCropShapes,
    aspects: List<ImageAspectRatio>? = DefaultImageCropperAspectRatios,
    autoZoom: Boolean = true,
): ImageCropperStyle = object : ImageCropperStyle {
    override val touchRad: Dp get() = touchRad
    override val backgroundColor: Color get() = backgroundColor
    override val overlayColor: Color get() = overlay
    override val shapes: List<ImageCropShape>? get() = shapes?.takeIf { it.isNotEmpty() }
    override val aspects get() = aspects
    override val autoZoom: Boolean get() = autoZoom
    override val enableRotationOption: Boolean get() = enableRotationOption
    override val enabledFlipOption: Boolean get() = enabledFlipOption

    override fun DrawScope.drawCropRect(region: Rect) {
        val strokeWidth = rectStrokeWidth.toPx()
        val finalRegion = region.inflate(strokeWidth / 2)
        if (finalRegion.isEmpty) return
        if (guidelines != null && guidelines.count > 0) {
            drawGuidelines(guidelines, finalRegion)
        }
        drawRect(
            color = rectColor, style = Stroke(strokeWidth),
            topLeft = finalRegion.topLeft, size = finalRegion.size
        )
        drawHandles(finalRegion)
    }

    override val handles: List<Offset> = if (!secondaryHandles) MainImageHandles else AllImageHandles

    private fun DrawScope.drawHandles(region: Rect) {
        val strokeWidth = (rectStrokeWidth * 3).toPx()
        val rad = touchRad.toPx() / 2
        val cap = StrokeCap.Round

        handles.forEach { (xRel, yRel) ->
            val x = region.left + xRel * region.width
            val y = region.top + yRel * region.height
            when {
                xRel != .5f && yRel != .5f -> {
                    drawCircle(color = rectColor, radius = rad, center = Offset(x, y))
                }
                xRel == 0f || xRel == 1f -> if (region.height > rad * 4) drawLine(
                    rectColor, strokeWidth = strokeWidth,
                    start = Offset(x, (y - rad)),
                    end = Offset(x, (y + rad)), cap = cap
                )
                yRel == 0f || yRel == 1f -> if (region.width > rad * 4) drawLine(
                    rectColor, strokeWidth = strokeWidth,
                    start = Offset((x - rad), y),
                    end = Offset((x + rad), y), cap = cap
                )
            }
        }
    }

    private fun DrawScope.drawGuidelines(
        guidelines: ImageCropperStyleGuidelines,
        region: Rect
    ) = clipRectangularRegion(rect = region) {
        val strokeWidth = guidelines.width.toPx()
        val xStep = region.width / (guidelines.count + 1)
        val yStep = region.height / (guidelines.count + 1)
        for (i in 1..guidelines.count) {
            val x = region.left + i * xStep
            val y = region.top + i * yStep
            drawLine(
                color = guidelines.color, strokeWidth = strokeWidth,
                start = Offset(x, 0f), end = Offset(x, size.height)
            )
            drawLine(
                color = guidelines.color, strokeWidth = strokeWidth,
                start = Offset(0f, y), end = Offset(size.width, y)
            )
        }
    }
}

private inline fun DrawScope.clipRectangularRegion(
    rect: Rect,
    op: ClipOp = ClipOp.Intersect,
    block: DrawScope.() -> Unit
) {
    clipRect(
        left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom,
        clipOp = op, block = block
    )
}
