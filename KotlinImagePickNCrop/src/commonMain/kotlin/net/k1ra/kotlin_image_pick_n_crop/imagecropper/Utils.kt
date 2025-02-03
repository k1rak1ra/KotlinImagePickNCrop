package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal interface InteractionState {
    val zoom: ZoomInteractionState
    val drag: DragInteractionState
    val tap: TapInteractionState
}

internal interface DragInteractionState {
    fun onBegin(x: Float, y: Float) = Unit
    fun onNext(dx: Float, dy: Float, x: Float, y: Float, pointers: Int) = Unit
    fun onDone() = Unit
}

internal inline fun createDragInteractionState(
    crossinline begin: (pos: Offset) -> Unit = { },
    crossinline done: () -> Unit = {},
    crossinline next: (delta: Offset, pos: Offset, pointers: Int) -> Unit = { _, _, _ -> },
): DragInteractionState = object : DragInteractionState {
    override fun onBegin(x: Float, y: Float) = begin(Offset(x, y))
    override fun onNext(dx: Float, dy: Float, x: Float, y: Float, pointers: Int) =
        next(Offset(dx, dy), Offset(x, y), pointers)

    override fun onDone() = done()
}

internal interface TapInteractionState {
    fun onTap(x: Float, y: Float, pointers: Int) = Unit
    fun onLongPress(x: Float, y: Float, pointers: Int) = Unit
}

internal interface ZoomInteractionState {
    fun onBegin(cx: Float, cy: Float) = Unit
    fun onNext(scale: Float, cx: Float, cy: Float) = Unit
    fun onDone() = Unit
}

internal inline fun ZoomInteractionState(
    crossinline begin: (center: Offset) -> Unit = { },
    crossinline done: () -> Unit = {},
    crossinline next: (scale: Float, center: Offset) -> Unit = { _, _ -> },
): ZoomInteractionState = object : ZoomInteractionState {
    override fun onBegin(cx: Float, cy: Float) = begin(Offset(cx, cy))
    override fun onNext(scale: Float, cx: Float, cy: Float) = next(scale, Offset(cx, cy))
    override fun onDone() = done()
}

@Composable
internal fun rememberInteractionState(
    zoom: ZoomInteractionState? = null,
    drag: DragInteractionState? = null,
    tap: TapInteractionState? = null,
): InteractionState {
    val zoomInteractionState by rememberUpdatedState(newValue = zoom ?: object :
        ZoomInteractionState {})
    val dragInteractionState by rememberUpdatedState(newValue = drag ?: object :
        DragInteractionState {})
    val tapInteractionState by rememberUpdatedState(newValue = tap ?: object :
        TapInteractionState {})
    return object : InteractionState {
        override val zoom: ZoomInteractionState get() = zoomInteractionState
        override val drag: DragInteractionState get() = dragInteractionState
        override val tap: TapInteractionState get() = tapInteractionState
    }
}

private data class InteractionData(
    var dragId: PointerId = PointerId(-1),
    var firstPos: Offset = Offset.Unspecified,
    var pos: Offset = Offset.Unspecified,
    var nextPos: Offset = Offset.Unspecified,
    var pointers: Int = 0,
    var maxPointers: Int = 0,
    var isDrag: Boolean = false,
    var isZoom: Boolean = false,
    var isTap: Boolean = false,
)


internal fun Modifier.onInteractions(state: InteractionState): Modifier {
    return pointerInput(Unit) {
        coroutineScope {
            var info = InteractionData()
            launch {
                detectTapGestures(
                    onLongPress = { state.tap.onLongPress(it.x, it.y, info.maxPointers) },
                    onTap = { state.tap.onTap(it.x, it.y, info.maxPointers) },
                )
            }
            launch {
                detectTransformGestures(panZoomLock = true) { c, _, zoom, _ ->
                    if (!(info.isDrag || info.isZoom)) {
                        if (info.pointers == 1) {
                            state.drag.onBegin(info.firstPos.x, info.firstPos.y)
                            info.pos = info.firstPos
                            info.isDrag = true
                        } else if (info.pointers > 1) {
                            state.zoom.onBegin(c.x, c.y)
                            info.isZoom = true
                        }
                    }
                    if (info.isDrag) {
                        state.drag.onNext(
                            info.nextPos.x - info.pos.x, info.nextPos.y - info.pos.y,
                            info.nextPos.x, info.nextPos.y, info.pointers
                        )
                        info.pos = info.nextPos
                    } else if (info.isZoom) {
                        if (zoom != 1f) state.zoom.onNext(zoom, c.x, c.y)
                    }
                }
            }
            launch {
                awaitEachGesture {
                    info = InteractionData()
                    val first = awaitFirstDown(requireUnconsumed = false)
                    info.dragId = first.id
                    info.firstPos = first.position
                    info.pointers = 1
                    info.maxPointers = 1
                    var event: PointerEvent
                    while (info.pointers > 0) {
                        event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        var dragPointer: PointerInputChange? = null
                        for (change in event.changes) {
                            if (change.changedToDown()) info.pointers++
                            else if (change.changedToUp()) info.pointers--
                            info.maxPointers = max(info.maxPointers, info.pointers)
                            if (change.id == info.dragId) dragPointer = change
                        }
                        if (dragPointer == null) dragPointer =
                            event.changes.firstOrNull { it.pressed }
                        if (dragPointer != null) {
                            info.nextPos = dragPointer.position
                            if (info.dragId != dragPointer.id) {
                                info.pos = info.nextPos
                                info.dragId = dragPointer.id
                            }
                        }
                    }
                    if (info.isDrag) state.drag.onDone()
                    if (info.isZoom) state.zoom.onDone()
                }
            }
        }
    }
}

private const val Eps: Float = 2.4414062E-4f

internal fun Float.eq0(): Boolean = abs(this) <= Eps
internal infix fun Float.eq(v: Float): Boolean = abs(v - this) <= Eps

internal fun lerpAngle(a: Int, b: Int, p: Float): Int {
    val angleDist = (2 * ((b - a) % 360) % 360 - (b - a) % 360)
    return (a + angleDist * p).roundToInt()
}

internal fun Int.next90() = (this + 90).angleRange()
internal fun Int.prev90() = (this - 90).angleRange()
internal fun Int.angleRange(): Int {
    val angle = (this % 360 + 360) % 360
    return if (angle <= 180) angle else angle - 360
}

fun Float.alignDown(alignment: Int): Float = floor(this / alignment) * alignment
fun Float.alignUp(alignment: Int): Float = ceil(this / alignment) * alignment
fun Float.align(alignment: Int): Float = round(this / alignment) * alignment


internal val IdentityMat = Matrix()

internal operator fun Matrix.times(other: Matrix): Matrix = copy().apply {
    this *= other
}

internal fun Matrix.setScaleTranslate(sx: Float, sy: Float, tx: Float, ty: Float) {
    reset()
    values[Matrix.ScaleX] = sx
    values[Matrix.TranslateX] = tx
    values[Matrix.ScaleY] = sy
    values[Matrix.TranslateY] = ty
}

internal fun Matrix.setRectToRect(src: Rect, dst: Rect) {
    val sx: Float = dst.width / src.width
    val tx = dst.left - src.left * sx
    val sy: Float = dst.height / src.height
    val ty = dst.top - src.top * sy
    setScaleTranslate(sx, sy, tx, ty)
}

internal fun Matrix.copy(): Matrix = Matrix(values.copyOf())

internal fun Matrix.inverted() = copy().apply { invert() }

fun polygonPath(
    tx: Float = 0f, ty: Float = 0f,
    sx: Float = 1f, sy: Float = 1f,
    points: FloatArray
): Path = Path().apply {
    if (points.size < 2) return@apply
    moveTo(points[0] * sx + tx, points[1] * sy + ty)
    for (i in 1 until points.size / 2) {
        lineTo(points[(i * 2) + 0] * sx + tx, points[(i * 2) + 1] * sy + ty)
    }
    close()
}

internal fun IntRect.toRect() = Rect(
    left = left.toFloat(), top = top.toFloat(),
    right = right.toFloat(), bottom = bottom.toFloat()
)

internal fun Size.coerceAtMost(maxSize: Size?): Size =
    if (maxSize == null) this else coerceAtMost(maxSize)

internal fun Size.coerceAtMost(maxSize: Size): Size {
    val scaleF = min(maxSize.width / width, maxSize.height / height)
    if (scaleF >= 1f) return this
    return Size(width = width * scaleF, height = height * scaleF)
}

internal fun Rect.atOrigin(): Rect = Rect(offset = Offset.Zero, size = size)

internal val Rect.area get() = width * height

internal fun Rect.lerp(target: Rect, p: Float): Rect {
    val tl0 = topLeft
    val br0 = bottomRight
    val dtl = target.topLeft - tl0
    val dbr = target.bottomRight - br0
    return Rect(tl0 + dtl * p, br0 + dbr * p)
}

internal fun Rect.centerIn(outer: Rect): Rect =
    translate(outer.center.x - center.x, outer.center.y - center.y)

internal fun Rect.fitIn(outer: Rect): Rect {
    val scaleF = min(outer.width / width, outer.height / height)
    return scale(scaleF, scaleF)
}

internal fun Rect.scale(sx: Float, sy: Float) = setSizeTL(width = width * sx, height = height * sy)

internal fun Rect.setSizeTL(width: Float, height: Float) =
    Rect(offset = topLeft, size = Size(width, height))

internal fun Rect.constrainResize(bounds: Rect): Rect = Rect(
    left = left.coerceAtLeast(bounds.left),
    top = top.coerceAtLeast(bounds.top),
    right = right.coerceAtMost(bounds.right),
    bottom = bottom.coerceAtMost(bounds.bottom),
)

internal fun Rect.constrainOffset(bounds: Rect): Rect {
    var (x, y) = topLeft
    if (right > bounds.right) x += bounds.right - right
    if (bottom > bounds.bottom) y += bounds.bottom - bottom
    if (x < bounds.left) x += bounds.left - x
    if (y < bounds.top) y += bounds.top - y
    return Rect(Offset(x, y), size)
}

internal fun Rect.resize(
    handle: Offset,
    delta: Offset,
): Rect {
    var (l, t, r, b) = this
    val (dx, dy) = delta
    if (handle.y == 1f) b += dy
    else if (handle.y == 0f) t += dy
    if (handle.x == 1f) r += dx
    else if (handle.x == 0f) l += dx
    if (l > r) l = r.also { r = l }
    if (t > b) t = b.also { b = t }
    return Rect(l, t, r, b)
}

internal fun Rect.roundOut(): IntRect = IntRect(
    left = floor(left).toInt(), top = floor(top).toInt(),
    right = ceil(right).toInt(), bottom = ceil(bottom).toInt()
)

internal fun Size.roundUp(): IntSize = IntSize(ceil(width).toInt(), ceil(height).toInt())

internal fun Rect.abs(rel: Offset): Offset {
    return Offset(left + rel.x * width, top + rel.y * height)
}

internal fun Rect.setAspect(aspect: ImageAspectRatio): Rect =
    setAspect(aspect.x.toFloat() / aspect.y)

internal fun Rect.setAspect(aspect: Float): Rect {
    val dim = max(width, height)
    return Rect(Offset.Zero, Size(dim * aspect, height = dim))
        .fitIn(this)
        .centerIn(this)
}

internal fun Size.keepAspect(old: Size): Size {
    val a = width * height
    return Size(
        width = sqrt((a * old.width) / old.height),
        height = sqrt((a * old.height) / old.width)
    )
}

internal fun Rect.keepAspect(old: Rect): Rect {
    return setSize(old, size.keepAspect(old.size))
}

internal fun Rect.setSize(old: Rect, size: Size): Rect {
    var (l, t, r, b) = this
    if ((old.left - l).absoluteValue < (old.right - r).absoluteValue) {
        r = l + size.width
    } else {
        l = r - size.width
    }
    if ((old.top - t).absoluteValue < (old.bottom - b).absoluteValue) {
        b = t + size.height
    } else {
        t = b - size.height
    }
    return Rect(l, t, r, b)
}

internal fun Rect.scaleToFit(bounds: Rect, old: Rect): Rect {
    val (l, t, r, b) = this
    val scale = minOf(
        (bounds.right - l) / (r - l),
        (bounds.bottom - t) / (b - t),
        (r - bounds.left) / (r - l),
        (bottom - bounds.top) / (b - t),
    )
    if (scale >= 1f) return this
    return setSize(old, size * scale)
}

internal fun IntRect.containsInclusive(other: IntRect): Boolean {
    return other.left >= left && other.top >= top &&
            other.right <= right && other.bottom <= bottom
}

internal fun Rect.align(alignment: Int): Rect = Rect(
    left.alignDown(alignment), top.alignDown(alignment),
    right.alignUp(alignment), bottom.alignUp(alignment)
)

@Stable
internal interface ViewMat {
    fun zoomStart(center: Offset)
    fun zoom(center: Offset, scale: Float)
    suspend fun fit(inner: Rect, outer: Rect)
    fun snapFit(inner: Rect, outer: Rect)
    val matrix: Matrix
    val invMatrix: Matrix
    val scale: Float
}

internal fun ViewMat() = object : ViewMat {
    var c0 = Offset.Zero
    var mat by mutableStateOf(Matrix(), neverEqualPolicy())
    val inv by derivedStateOf {
        Matrix().apply {
            setFrom(mat)
            invert()
        }
    }
    override val scale by derivedStateOf {
        mat.values[Matrix.ScaleX]
    }

    override fun zoomStart(center: Offset) {
        c0 = center
    }

    override fun zoom(center: Offset, scale: Float) {
        val s = Matrix().apply {
            translate(center.x - c0.x, center.y - c0.y)
            translate(center.x, center.y)
            scale(scale, scale)
            translate(-center.x, -center.y)
        }
        update { it *= s }
        c0 = center
    }

    inline fun update(op: (Matrix) -> Unit) {
        mat = mat.copy().also(op)
    }

    override val matrix: Matrix
        get() = mat
    override val invMatrix: Matrix
        get() = inv

    override suspend fun fit(inner: Rect, outer: Rect) {
        val dst = getDst(inner, outer) ?: return
        val mat = Matrix()
        val initial = this.mat.copy()
        animate(0f, 1f) { p, _ ->
            update {
                it.setFrom(initial)
                it *= mat.apply { setRectToRect(inner, inner.lerp(dst, p)) }
            }
        }
    }

    override fun snapFit(inner: Rect, outer: Rect) {
        val dst = getDst(inner, outer) ?: return
        update { it *= Matrix().apply { setRectToRect(inner, dst) } }
    }

    private fun getDst(inner: Rect, outer: Rect): Rect? {
        val scale = min(outer.width / inner.width, outer.height / inner.height)
        return Rect(Offset.Zero, inner.size * scale).centerIn(outer)
    }

}