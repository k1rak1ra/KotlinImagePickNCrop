package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImageTransformation(val angleDeg: Int, val scale: Offset, val pivotRel: Offset) {
    internal val hasTransform get() = angleDeg != 0 || scale != Offset(1f, 1f)

    companion object {
        @Stable
        internal val Identity = ImageTransformation(0, Offset(1f, 1f), Offset(.5f, .5f))
    }
}

internal fun ImageTransformation.toMatrix(imgSize: IntSize): Matrix {
    if (!hasTransform) return IdentityMat
    val matrix = Matrix()
    val pivot = Offset(imgSize.width * pivotRel.x, imgSize.height * pivotRel.y)
    matrix.translate(pivot.x, pivot.y)
    matrix.rotateZ(angleDeg.toFloat())
    matrix.scale(scale.x, scale.y)
    matrix.translate(-pivot.x, -pivot.y)
    return matrix
}

private val MoveImageHandle = Offset(.5f, .5f)

internal class DraggableHandle(
    val handle: Offset,
    val initialPos: Offset,
    val initialRegion: Rect
)

internal fun Modifier.cropperInteractionModifier(
    region: Rect,
    onRegion: (Rect) -> Unit,
    touchRad: Dp,
    handles: List<Offset>,
    viewMat: ViewMat,
    pending: DraggableHandle?,
    onPending: (DraggableHandle?) -> Unit,
): Modifier = composed {
    val touchRadPx2 = LocalDensity.current.run {
        remember(touchRad, viewMat.scale) { touchRad.toPx() / viewMat.scale }.let { it * it }
    }

    onInteractions(
        rememberInteractionState(
            zoom = ZoomInteractionState(
                begin = { c -> viewMat.zoomStart(c) },
                next = { s, c -> viewMat.zoom(c, s) },
            ),
            drag = createDragInteractionState(
                begin = { pos ->
                    val localPos = viewMat.invMatrix.map(pos)
                    handles.findClosestHandle(
                        region, localPos,
                        touchRadPx2
                    )?.let { handle ->
                        onPending(DraggableHandle(handle, localPos, region))
                    }
                },
                next = { _, pos, _ ->
                    pending?.let {
                        val localPos = viewMat.invMatrix.map(pos)
                        val delta = (localPos - pending.initialPos).round().toOffset()
                        val newRegion = if (pending.handle != MoveImageHandle) {
                            pending.initialRegion
                                .resize(pending.handle, delta)
                        } else {
                            pending.initialRegion.translate(delta)
                        }
                        onRegion(newRegion)
                    }
                },
                done = {
                    onPending(null)
                })
        )
    )
}

private fun List<Offset>.findClosestHandle(
    region: Rect,
    pos: Offset,
    touchRadPx2: Float
): Offset? {
    firstOrNull { (region.abs(it) - pos).getDistanceSquared() <= touchRadPx2 }?.let { return it }
    if (region.contains(pos)) return MoveImageHandle
    return null
}

@Composable
internal fun animateImageTransformation(target: ImageTransformation): State<ImageTransformation> {
    var prev by remember { mutableStateOf<ImageTransformation?>(null) }
    val current = remember { mutableStateOf(target) }
    LaunchedEffect(target) {
        val a = prev
        try {
            if (a != null) animate(0f, 1f) { p, _ ->
                current.value = (a.lerp(target, p))
            }
        } finally {
            current.value = (target)
            prev = target
        }
    }
    return current
}

private fun ImageTransformation.lerp(target: ImageTransformation, p: Float): ImageTransformation {
    if (p == 0f) return this
    if (p == 1f) return target
    return ImageTransformation(
        angleDeg = lerpAngle(angleDeg, target.angleDeg, p),
        scale = androidx.compose.ui.geometry.lerp(scale, target.scale, p),
        pivotRel = androidx.compose.ui.geometry.lerp(pivotRel, target.pivotRel, p)
    )
}

internal suspend fun ImageCropState.generateResult(
    maxSize: IntSize?
): ImageBitmap? = withContext(Dispatchers.Default) {
    runCatching { doGenerateResult(maxSize) }
        .onFailure { it.printStackTrace() }
        .getOrNull()
}

private suspend fun ImageCropState.doGenerateResult(maxSize: IntSize?): ImageBitmap? {
    val finalSize = region.size
        .coerceAtMost(maxSize?.toSize())
        .roundUp()
    val result = ImageBitmap(finalSize.width, finalSize.height)
    val canvas = Canvas(result)
    val viewMat = ViewMat()
    viewMat.snapFit(region, finalSize.toSize().toRect())
    val imgMat = transform.toMatrix(src.size)
    val totalMat = imgMat * viewMat.matrix

    canvas.clipPath(shape.asPath(region.atOrigin()))
    canvas.concat(totalMat)
    val inParams = deriveDecodeParameters(view = finalSize, img = src.size, totalMat)
        ?: return null
    val decoded = src.open(inParams) ?: return null
    val paint = Paint().apply { filterQuality = FilterQuality.High }
    canvas.drawImageRect(
        image = decoded.bmp, paint = paint,
        dstOffset = decoded.params.subset.topLeft,
        dstSize = decoded.params.subset.size,
    )
    return result
}