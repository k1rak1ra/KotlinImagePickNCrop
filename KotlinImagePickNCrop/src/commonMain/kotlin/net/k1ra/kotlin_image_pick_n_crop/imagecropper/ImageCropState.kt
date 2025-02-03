package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toSize

@Stable
interface ImageCropState {
    val src: ImgSrc
    var transform: ImageTransformation
    var region: Rect
    var aspectLock: Boolean
    var shape: ImageCropShape
    val accepted: Boolean
    fun done(accept: Boolean)
    fun reset()
}

internal fun ImageCropState(
    src: ImgSrc,
    onDone: () -> Unit = {},
): ImageCropState = object : ImageCropState {
    val defaultTransform: ImageTransformation = ImageTransformation.Identity
    val defaultShape: ImageCropShape = RectImgCropShape
    val defaultAspectLock: Boolean = false
    override val src: ImgSrc get() = src
    private var _transform: ImageTransformation by mutableStateOf(defaultTransform)
    override var transform: ImageTransformation
        get() = _transform
        set(value) {
            onTransformUpdated(transform, value)
            _transform = value
        }

    val defaultRegion = src.size.toSize().toRect()

    private var _region by mutableStateOf(defaultRegion)
    override var region
        get() = _region
        set(value) {
            _region = updateRegion(
                old = _region, new = value,
                bounds = imgRect, aspectLock = aspectLock
            )
        }

    val imgRect by derivedStateOf { getTransformedImageRect(transform, src.size) }

    override var shape: ImageCropShape by mutableStateOf(defaultShape)
    override var aspectLock by mutableStateOf(defaultAspectLock)

    private fun onTransformUpdated(old: ImageTransformation, new: ImageTransformation) {
        val unTransform = old.toMatrix(src.size).apply { invert() }
        _region = new.toMatrix(src.size).map(unTransform.map(region))
    }

    override fun reset() {
        transform = defaultTransform
        shape = defaultShape
        _region = defaultRegion
        aspectLock = defaultAspectLock
    }

    override var accepted: Boolean by mutableStateOf(false)

    override fun done(accept: Boolean) {
        accepted = accept
        onDone()
    }
}

internal fun getTransformedImageRect(transform: ImageTransformation, size: IntSize): Rect {
    val dstMat = transform.toMatrix(size)
    return dstMat.map(size.toIntRect().toRect())
}

internal fun ImageCropState.rotateLeft() {
    transform = transform.copy(angleDeg = transform.angleDeg.prev90())
}

internal fun ImageCropState.rotateRight() {
    transform = transform.copy(angleDeg = transform.angleDeg.next90())
}

internal fun ImageCropState.flipHorizontal() {
    if ((transform.angleDeg / 90) % 2 == 0) flipX() else flipY()
}

internal fun ImageCropState.flipVertical() {
    if ((transform.angleDeg / 90) % 2 == 0) flipY() else flipX()
}

internal fun ImageCropState.flipX() {
    transform = transform.copy(scale = transform.scale.copy(x = -1 * transform.scale.x))
}

internal fun ImageCropState.flipY() {
    transform = transform.copy(scale = transform.scale.copy(y = -1 * transform.scale.y))
}

internal fun updateRegion(old: Rect, new: Rect, bounds: Rect, aspectLock: Boolean): Rect {
    val offsetOnly = old.width.eq(new.width) && old.height.eq(new.height)
    return if (offsetOnly) new.constrainOffset(bounds)
    else {
        val result = when {
            aspectLock -> new.keepAspect(old).scaleToFit(bounds, old)
            else -> new.constrainResize(bounds)
        }
        return when {
            result.isEmpty -> result.setSize(old, Size(1f, 1f)).constrainOffset(bounds)
            else -> result
        }
    }
}